package com.kepo.api;

import com.kepo.controller.DashboardController;
import com.kepo.controller.InventoryController;
import com.kepo.controller.LoginController;
import com.kepo.controller.RefugeeShelterController;
import com.kepo.model.*;
import com.kepo.repository.*;
import com.kepo.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RestApiServer {

    private final int port;
    private final ObjectMapper json;
    private final HttpServer server;

    private final UserService userService;
    private final EventService eventService;
    private final ShelterService shelterService;
    private final RefugeeService refugeeService;
    private final InventoryService inventoryService;
    private final DistributionService distributionService;
    private final DonorService donorService;
    private final SupplierService supplierService;
    private final ReportService reportService;
    private final BarcodeService barcodeService;
    private final AIRecommendationService aiRecommendationService;
    private final ShelterStockService shelterStockService;
    private final LoginController loginController;
    private final DashboardController dashboardController;
    private final InventoryController inventoryController;
    private final RefugeeShelterController refugeeShelterController;
    private final MedicineRepository medicineRepo;
    private final ShelterRepository shelterRepo;
    private final MedicineRequestRepository medicineRequestRepo;

    public RestApiServer(int port, ObjectMapper json,
                         UserService userService, EventService eventService,
                         ShelterService shelterService, RefugeeService refugeeService,
                         InventoryService inventoryService, DistributionService distributionService,
                         DonorService donorService, SupplierService supplierService,
                         ReportService reportService, BarcodeService barcodeService,
                         AIRecommendationService aiRecommendationService,
                         ShelterStockService shelterStockService,
                         LoginController loginController, DashboardController dashboardController,
                         InventoryController inventoryController,
                         RefugeeShelterController refugeeShelterController,
                         MedicineRepository medicineRepo, ShelterRepository shelterRepo,
                         MedicineRequestRepository medicineRequestRepo) throws IOException {
        this.port = port;
        this.json = json;
        this.userService = userService;
        this.eventService = eventService;
        this.shelterService = shelterService;
        this.refugeeService = refugeeService;
        this.inventoryService = inventoryService;
        this.distributionService = distributionService;
        this.donorService = donorService;
        this.supplierService = supplierService;
        this.reportService = reportService;
        this.barcodeService = barcodeService;
        this.aiRecommendationService = aiRecommendationService;
        this.shelterStockService = shelterStockService;
        this.loginController = loginController;
        this.dashboardController = dashboardController;
        this.inventoryController = inventoryController;
        this.refugeeShelterController = refugeeShelterController;
        this.medicineRepo = medicineRepo;
        this.shelterRepo = shelterRepo;
        this.medicineRequestRepo = medicineRequestRepo;

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
    }

    private void setupRoutes() {
        server.createContext("/api/auth/login", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if (!"POST".equals(method)) return error(405, "Method not allowed");
                ObjectNode node = (ObjectNode) json.readTree(body);
                return loginController.login(node.get("username").asText(), node.get("password").asText());
            }
        });

        server.createContext("/api/auth/logout", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        // Dashboard
        server.createContext("/api/dashboard/stats", exchange -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalShelters", dashboardController.getTotalSheltersCount());
            stats.put("totalRefugees", dashboardController.getTotalRefugeesCount());
            stats.put("criticalShelters", dashboardController.getCriticalSheltersCount());
            stats.put("activeEvents", dashboardController.getActiveEventsCount());
            stats.put("fullShelters", dashboardController.getFullSheltersCount());
            stats.put("criticalLogistics", dashboardController.getCriticalLogisticsSheltersCount());
            writeJson(exchange, 200, stats);
        });

        server.createContext("/api/dashboard/alerts", exchange -> {
            List<String> alerts = dashboardController.getEmergencyAlerts();
            writeJson(exchange, 200, alerts);
        });

        server.createContext("/api/dashboard/distributions", exchange -> {
            List<Distribution> dists = dashboardController.getDistributions();
            writeJson(exchange, 200, dists);
        });

        server.createContext("/api/dashboard/ai-suggestions", exchange -> {
            List<String> suggestions = dashboardController.getAISuggestions();
            writeJson(exchange, 200, suggestions);
        });

        // CRUD routes
        registerCrud("/api/events", eventService);
        registerCrud("/api/shelters", shelterService);
        registerCrud("/api/medicines", inventoryService);
        registerCrud("/api/distributions", distributionService);
        registerCrud("/api/suppliers", supplierService);
        registerCrud("/api/donors", donorService);
        registerCrud("/api/users", userService);

        // Refugees
        server.createContext("/api/refugees", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if ("GET".equals(method)) return refugeeService.getAllRefugees();
                if ("POST".equals(method)) {
                    Refugee r = json.readValue(body, Refugee.class);
                    return refugeeService.saveRefugee(r) ? r : error(500, "Save failed");
                }
                if ("DELETE".equals(method)) {
                    int id = Integer.parseInt(params.get("id"));
                    return refugeeService.deleteRefugee(id);
                }
                return error(405, "Method not allowed");
            }
        });

        server.createContext("/api/refugees/", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                String path = params.get("__path");
                if (path == null) return error(400, "Bad request");
                String[] parts = path.split("/");
                if (parts.length < 2) return error(400, "Bad request");
                int id = Integer.parseInt(parts[0]);
                String action = parts.length > 1 ? parts[1] : "";

                if ("checkin".equals(action) && "POST".equals(method)) {
                    ObjectNode node = (ObjectNode) json.readTree(body);
                    int shelterId = node.get("shelterId").asInt();
                    Refugee r = refugeeService.getRefugeeById(id);
                    return refugeeService.checkIn(r, shelterId);
                }
                if ("checkout".equals(action) && "POST".equals(method)) {
                    Refugee r = refugeeService.getRefugeeById(id);
                    return refugeeService.checkOut(r);
                }
                if ("transfer".equals(action) && "POST".equals(method)) {
                    ObjectNode node = (ObjectNode) json.readTree(body);
                    int targetId = node.get("targetShelterId").asInt();
                    String notes = node.has("notes") ? node.get("notes").asText() : "";
                    return refugeeShelterController.transferRefugee(id, targetId, notes);
                }
                if ("movements".equals(action)) {
                    return refugeeShelterController.getMovementHistory(id);
                }
                return error(404, "Not found");
            }
        });

        // Medicine stock adjustment
        server.createContext("/api/medicines/", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                String path = params.get("__path");
                if (path == null || !path.contains("/stock")) return error(404, "Not found");
                int id = Integer.parseInt(path.split("/")[0]);
                ObjectNode node = (ObjectNode) json.readTree(body);
                int qty = node.get("qty").asInt();
                String type = node.get("type").asText();
                String notes = node.has("notes") ? node.get("notes").asText() : "";
                if (type.startsWith("IN")) return inventoryController.addStock(id, qty, notes);
                if (type.startsWith("OUT")) return inventoryController.reduceStock(id, qty, notes);
                return inventoryController.adjustStock(id, qty, notes);
            }
        });

        // Distribution status update
        server.createContext("/api/distributions/", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                String path = params.get("__path");
                if (path == null || !path.contains("/status")) return error(404, "Not found");
                int id = Integer.parseInt(path.split("/")[0]);
                ObjectNode node = (ObjectNode) json.readTree(body);
                String status = node.get("status").asText();
                Distribution d = distributionService.getDistributionById(id);
                if (d == null) return error(404, "Not found");
                d.setStatus(status);
                return distributionService.saveDistribution(d);
            }
        });

        // Barcode: generate image
        server.createContext("/api/barcode/generate", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); exchange.close(); return;
            }
            String query = exchange.getRequestURI().getRawQuery();
            String code = null;
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2 && "code".equals(kv[0])) {
                        code = java.net.URLDecoder.decode(kv[1], "UTF-8");
                    }
                }
            }
            if (code == null || code.isBlank()) {
                Map<String, String> err = new HashMap<>(); err.put("error", "Parameter 'code' diperlukan");
                writeJson(exchange, 400, err); return;
            }
            java.awt.image.BufferedImage img = barcodeService.generateBarcode(code);
            if (img == null) {
                Map<String, String> err = new HashMap<>(); err.put("error", "Gagal generate barcode");
                writeJson(exchange, 500, err); return;
            }
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, 0);
            javax.imageio.ImageIO.write(img, "PNG", exchange.getResponseBody());
            exchange.getResponseBody().close();
        });

        // Barcode: decode image
        server.createContext("/api/barcode/decode", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if (!"POST".equals(method)) return error(405, "Method not allowed");
                ObjectNode node = (ObjectNode) json.readTree(body);
                String imageData = node.has("image") ? node.get("image").asText() : "";
                if (imageData.isBlank()) return error(400, "Image data required");
                // Decode base64 image
                String base64Data = imageData.contains(",") ? imageData.split(",")[1] : imageData;
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
                if (img == null) return error(400, "Cannot decode image");
                String code = barcodeService.decodeBarcode(img);
                Map<String, String> res = new HashMap<>();
                if (code != null) {
                    res.put("code", code);
                } else {
                    res.put("code", "");
                    res.put("error", "Barcode tidak terdeteksi");
                }
                return res;
            }
        });

        // Barcode: lookup medicine by barcode code
        server.createContext("/api/barcode/lookup", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if (!"GET".equals(method)) return error(405, "Method not allowed");
                String code = params.get("code");
                if (code == null || code.isBlank()) return error(400, "Parameter 'code' diperlukan");
                Medicine m = barcodeService.lookupMedicine(code);
                if (m == null) return error(404, "Obat tidak ditemukan untuk kode: " + code);
                return m;
            }
        });

        // Reports
        server.createContext("/api/reports", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                String type = params.get("type");
                String format = params.get("format");
                if (type == null || format == null) return error(400, "Missing type or format");
                String filePath;
                switch (type) {
                    case "shelter": filePath = reportService.generateShelterReport(format); break;
                    case "refugee": filePath = reportService.generateRefugeeReport(format); break;
                    case "medicine":
                    case "inventory": filePath = reportService.generateInventoryReport(format); break;
                    case "distribution": filePath = reportService.generateDistributionReport(format); break;
                    case "donor": filePath = reportService.generateDonorReport(format); break;
                    case "operation":
                    case "event": filePath = reportService.generateEventReport(format); break;
                    default: return error(400, "Invalid report type");
                }
                Map<String, String> result = new HashMap<>();
                result.put("filePath", filePath);
                return result;
            }
        });

        // Auto-distribution suggestions (approve/reject workflow)
        server.createContext("/api/auto-distributions/suggestions", exchange -> {
            List<String> lacking = aiRecommendationService.getLackingLogisticsAnalysis();
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < lacking.size() && i < 8; i++) {
                Map<String, Object> sug = new HashMap<>();
                sug.put("id", i + 1);
                sug.put("description", lacking.get(i));
                sug.put("status", "PENDING");
                result.add(sug);
            }
            if (result.isEmpty()) {
                Map<String, Object> sug = new HashMap<>();
                sug.put("id", 1);
                sug.put("description", "Semua shelter terpantau memiliki pasokan obat yang memadai.");
                sug.put("status", "NONE");
                result.add(sug);
            }
            writeJson(exchange, 200, result);
        });

        server.createContext("/api/auto-distributions/approve", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if (!"POST".equals(method)) return error(405, "Method not allowed");
                ObjectNode node = (ObjectNode) json.readTree(body);
                Distribution d = new Distribution();
                d.setDocNum("AUTO-" + System.currentTimeMillis());
                d.setShelterId(node.has("shelterId") ? node.get("shelterId").asInt() : 1);
                d.setItemType(node.has("itemType") ? node.get("itemType").asText() : "OBAT");
                d.setQuantity(node.has("quantity") ? node.get("quantity").asInt() : 20);
                d.setStatus("APPROVED");
                String medName = node.has("medicineName") ? node.get("medicineName").asText() : "Obat";
                String shelterName = node.has("shelterName") ? node.get("shelterName").asText() : "Shelter";

                // Look up medicine code by name for allocation tracking
                String medCode = "";
                List<Medicine> allMeds = medicineRepo.findAll();
                for (Medicine m : allMeds) {
                    if (medName.toLowerCase().contains(m.getMedicineName().toLowerCase())
                        || m.getMedicineName().toLowerCase().contains(medName.toLowerCase())) {
                        medCode = m.getMedicineCode();
                        break;
                    }
                }

                // Look up shelterId by name if not already set
                if (d.getShelterId() <= 1) {
                    List<Shelter> allShelters = shelterRepo.findAll();
                    for (Shelter s : allShelters) {
                        if (shelterName.toLowerCase().contains(s.getName().toLowerCase())
                            || s.getName().toLowerCase().contains(shelterName.toLowerCase())) {
                            d.setShelterId(s.getShelterId());
                            break;
                        }
                    }
                }

                // AI diagnosis
                String analysis = "";
                try {
                    String prompt = "Berikan analisis medis dan rekomendasi singkat (2-3 kalimat) dalam Bahasa Indonesia untuk keputusan distribusi obat berikut:\n"
                        + "- Shelter: " + shelterName + "\n"
                        + "- Obat: " + medName + "\n"
                        + "- Jumlah: " + d.getQuantity() + " unit\n"
                        + "Jelaskan mengapa distribusi ini penting, apa dampaknya bagi pengungsi, dan saran monitoring ke depan. Jangan gunakan emoji.";
                    analysis = aiRecommendationService.chat(prompt);
                } catch (Exception e) {
                    analysis = "Analisis AI tidak tersedia.";
                }

                // Save with allocation data so getLackingLogisticsAnalysis() can detect it
                String allocationData = medCode.isEmpty() ? "" : "\n[ALLOCATION_DATA:" + medCode + ":" + d.getQuantity() + "]";
                d.setNotes("REKOMENDASI AI OTOMATIS\nObat: " + medName + "\nShelter: " + shelterName + "\nJumlah: " + d.getQuantity()
                    + "\n\nAnalisis AI:\n" + analysis + allocationData);

                distributionService.saveDistribution(d);

                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("distributionId", d.getDistributionId());
                res.put("docNum", d.getDocNum());
                res.put("analysis", analysis);
                return res;
            }
        });

        // AI diagnosis endpoint with full RAG context
        server.createContext("/api/ai/analyze", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if (!"POST".equals(method)) return error(405, "Method not allowed");
                ObjectNode node = (ObjectNode) json.readTree(body);
                String query = node.has("query") ? node.get("query").asText() : "";
                if (query.isBlank()) return error(400, "Query diperlukan");

                String response = aiRecommendationService.chat(query);
                Map<String, String> res = new HashMap<>();
                res.put("response", response);
                return res;
            }
        });

        // Medicine Requests (per-user / per-refugee)
        server.createContext("/api/medicine-requests", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if ("GET".equals(method)) {
                    String shelterParam = params.get("shelterId");
                    if (shelterParam != null) {
                        return medicineRequestRepo.findByShelterId(Integer.parseInt(shelterParam));
                    }
                    return medicineRequestRepo.findAll();
                } else if ("POST".equals(method)) {
                    ObjectNode node = (ObjectNode) json.readTree(body);
                    MedicineRequest req = new MedicineRequest();
                    req.setRefugeeId(node.get("refugeeId").asInt());
                    req.setShelterId(node.get("shelterId").asInt());
                    req.setMedicineCode(node.has("medicineCode") ? node.get("medicineCode").asText() : "");
                    req.setMedicineName(node.get("medicineName").asText());
                    req.setQuantity(node.has("quantity") ? node.get("quantity").asInt() : 1);
                    req.setStatus("PENDING");
                    req.setNotes(node.has("notes") ? node.get("notes").asText() : "");
                    medicineRequestRepo.save(req);
                    Map<String, Object> res = new HashMap<>();
                    res.put("success", true);
                    res.put("requestId", req.getRequestId());
                    return res;
                } else {
                    return error(405, "Method not allowed");
                }
            }
        });

        // Update Medicine Request status (approve/reject/fulfill)
        server.createContext("/api/medicine-requests/status", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if (!"POST".equals(method)) return error(405, "Method not allowed");
                ObjectNode node = (ObjectNode) json.readTree(body);
                int requestId = node.get("requestId").asInt();
                String status = node.get("status").asText();
                String notes = node.has("notes") ? node.get("notes").asText() : "";
                boolean ok = medicineRequestRepo.updateStatus(requestId, status, notes);
                Map<String, Object> res = new HashMap<>();
                res.put("success", ok);
                return res;
            }
        });

        // Medicine Requests count for dashboard
        server.createContext("/api/medicine-requests/count", exchange -> {
            List<MedicineRequest> all = medicineRequestRepo.findAll();
            int pending = 0;
            int approved = 0;
            int fulfilled = 0;
            for (MedicineRequest r : all) {
                switch (r.getStatus()) {
                    case "PENDING": pending++; break;
                    case "APPROVED": approved++; break;
                    case "FULFILLED": fulfilled++; break;
                }
            }
            Map<String, Object> res = new HashMap<>();
            res.put("pending", pending);
            res.put("approved", approved);
            res.put("fulfilled", fulfilled);
            res.put("total", all.size());
            writeJson(exchange, 200, res);
        });

        // AI Chat
        server.createContext("/api/ai/chat", new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                if (!"POST".equals(method)) return error(405, "Method not allowed");
                ObjectNode node = (ObjectNode) json.readTree(body);
                String response = aiRecommendationService.chat(node.get("message").asText());
                Map<String, String> result = new HashMap<>();
                result.put("response", response);
                return result;
            }
        });

        // Predictions
        server.createContext("/api/predictions", exchange -> {
            Map<String, Object> result = new HashMap<>();
            result.put("summary", aiRecommendationService.getSituationalExecutiveSummary());
            result.put("shelterForecasts", aiRecommendationService.getShelterOccupancyPredictions());
            result.put("priorities", aiRecommendationService.getPriorityShelters());
            result.put("medPredictions", aiRecommendationService.getMedicineStockDepletionPredictions());
            result.put("lackingLogistics", aiRecommendationService.getLackingLogisticsAnalysis());
            writeJson(exchange, 200, result);
        });
    }

    private void registerCrud(String basePath, Object service) {
        server.createContext(basePath, new JsonHandler() {
            @Override protected Object handle(String method, Map<String, String> params, String body) throws Exception {
                switch (method) {
                    case "GET": {
                        if (service instanceof EventService) return ((EventService) service).getAllEvents();
                        if (service instanceof ShelterService) return ((ShelterService) service).getAllShelters();
                        if (service instanceof InventoryService) return ((InventoryService) service).getAllMedicines();
                        if (service instanceof DistributionService) return ((DistributionService) service).getAllDistributions();
                        if (service instanceof SupplierService) return ((SupplierService) service).getAllSuppliers();
                        if (service instanceof DonorService) return ((DonorService) service).getAllDonors();
                        if (service instanceof UserService) return ((UserService) service).getAllUsers();
                        return error(500, "Unknown service");
                    }
                    case "POST": {
                        if (service instanceof EventService) return handleSave(((EventService) service), body, Event.class);
                        if (service instanceof ShelterService) return handleSave(((ShelterService) service), body, Shelter.class);
                        if (service instanceof InventoryService) return handleSave(((InventoryService) service), body, Medicine.class);
                        if (service instanceof DistributionService) return handleSaveDistribution(body);
                        if (service instanceof SupplierService) return handleSave(((SupplierService) service), body, Supplier.class);
                        if (service instanceof DonorService) return handleSave(((DonorService) service), body, Donor.class);
                        if (service instanceof UserService) return handleSaveUser(body);
                        return error(500, "Unknown service");
                    }
                    case "DELETE": {
                        int id = Integer.parseInt(params.get("id"));
                        if (service instanceof EventService) return ((EventService) service).deleteEvent(id);
                        if (service instanceof ShelterService) return ((ShelterService) service).deleteShelter(id);
                        if (service instanceof InventoryService) return ((InventoryService) service).deleteMedicine(id);
                        if (service instanceof DistributionService) return ((DistributionService) service).deleteDistribution(id);
                        if (service instanceof SupplierService) return ((SupplierService) service).deleteSupplier(id);
                        if (service instanceof DonorService) return ((DonorService) service).deleteDonor(id);
                        if (service instanceof UserService) return ((UserService) service).deleteUser(id);
                        return error(500, "Unknown service");
                    }
                    default: return error(405, "Method not allowed");
                }
            }
        });
    }

    private <T> Object handleSave(Object service, String body, Class<T> clazz) throws Exception {
        T entity = json.readValue(body, clazz);
        if (service instanceof EventService) return ((EventService) service).saveEvent((Event) entity);
        if (service instanceof ShelterService) return ((ShelterService) service).saveShelter((Shelter) entity);
        if (service instanceof InventoryService) return ((InventoryService) service).saveMedicine((Medicine) entity);
        if (service instanceof SupplierService) return ((SupplierService) service).saveSupplier((Supplier) entity);
        if (service instanceof DonorService) return ((DonorService) service).saveDonor((Donor) entity);
        return error(500, "Save failed");
    }

    private Object handleSaveDistribution(String body) throws Exception {
        Distribution d = json.readValue(body, Distribution.class);
        return distributionService.saveDistribution(d);
    }

    private Object handleSaveUser(String body) throws Exception {
        ObjectNode node = (ObjectNode) json.readTree(body);
        String u = node.has("username") ? node.get("username").asText() : "";
        String pwd = node.has("password") ? node.get("password").asText() : "";
        String name = node.has("fullName") ? node.get("fullName").asText() : "";
        String role = node.has("role") ? node.get("role").asText() : "SHELTER_OFFICER";
        return userService.saveUserFromApi(u, pwd, name, role);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    // --- JSON helpers ---

    private Map<String, String> error(int code, String msg) {
        Map<String, String> err = new HashMap<>();
        err.put("error", msg);
        return err;
    }

    private void writeJson(HttpExchange exchange, int status, Object data) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        byte[] bytes = json.writeValueAsBytes(data);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private abstract class JsonHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();
                String query = exchange.getRequestURI().getRawQuery();

                Map<String, String> params = new HashMap<>();
                if (query != null) {
                    for (String pair : query.split("&")) {
                        String[] kv = pair.split("=", 2);
                        if (kv.length == 2) params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                    }
                }

                // Extract path params: /api/events/123 -> params.id=123
                String base = path.substring(path.indexOf("/", 1) + 1);
                if (base.contains("/")) {
                    String basePath = base.substring(0, base.indexOf("/"));
                    String rest = base.substring(base.indexOf("/") + 1);
                    if (!rest.contains("/")) {
                        params.put("id", rest);
                    } else {
                        params.put("id", rest.substring(0, rest.indexOf("/")));
                        params.put("__path", rest);
                    }
                }

                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                Object result = handle(method, params, body);

                if (result instanceof Map && ((Map) result).containsKey("error")) {
                    writeJson(exchange, ((Map) result).get("error").equals("Method not allowed") ? 405 : 400, result);
                } else {
                    writeJson(exchange, 200, result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, String> err = new HashMap<>();
                err.put("error", e.getMessage());
                writeJson(exchange, 500, err);
            }
        }

        protected abstract Object handle(String method, Map<String, String> params, String body) throws Exception;
    }
}
