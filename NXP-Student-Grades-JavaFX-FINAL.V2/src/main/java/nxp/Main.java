package nxp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.transform.Scale;
import javafx.stage.*;
import java.util.*;
import java.util.function.Function;

/** NXP Santic - Student Grade Management. Layout is absolute, measured from the 1280x800 Figma frames. */
public class Main extends Application {
    record Subj(String code, String title, Integer mid, Integer fin) {
        Integer overall() { return mid == null || fin == null ? null : (int) Math.round((mid + fin) / 2.0); }
    }
    static final String EMAIL = "mariomimay12@gmail.com", PASS = "mario123";
    static final double DW = 1600, DX = DW - 1280, F = (DW - 256) / 1024.0;   // app design width (smaller UI), right-edge shift, summary spread
    static final String[] SEM_NAMES = {"First Semester", "Second Semester"};
    static final List<List<Subj>> SEM = List.of(
        List.of(new Subj("IT-100", "Introduction to Computing", 90, 96), new Subj("IT-101", "Computer Programming", 93, 93),
                new Subj("GEC1", "Understanding the Self", 92, 92), new Subj("GEC2", "Readings in Philippine History", 89, 89),
                new Subj("GEC3", "Mathematics in the Modern World", 88, 94), new Subj("PATHFit1", "Movement of Competency Training", 90, 96)),
        List.of(new Subj("IT-102", "Computer Programming 2", null, null), new Subj("IT-103", "Data Structures and Algorithms", null, null),
                new Subj("IT-104", "Discrete Mathematics", null, null), new Subj("GEC4", "Purposive Communication", null, null),
                new Subj("GEC5", "Art Appreciation", null, null), new Subj("GE ELEC 7", "Environmental Science", null, null),
                new Subj("PATHFit 2", "Exercise-based Fitness Activities", null, null)));
    // design colours sampled from the prototype
    static final Color DIM = Color.web("#8fa8ba"), TXT = Color.web("#9db3c4"), CY = Color.web("#00d0ff"), LINE = Color.web("#193a5b"), CARD = Color.web("#00223b");

    Stage stage; StackPane host;
    String page = "dash", period = "Midterm", search = "", filter = ""; int sem = 0;
    boolean gradesOpen; int subOpen = -1; String popup = "";
    TextField searchField; double scroll;

    String screen = "login"; double dh = 800; final List<Node> late = new ArrayList<>();
    public void start(Stage s) {
        stage = s; s.setTitle("NXP Santic - Student Grade Management");
        host = new StackPane(); host.setStyle("-fx-background-color:#06172f;");
        Scene sc = new Scene(host, 1280, 800); sc.getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        s.getIcons().add(img("logo")); s.setScene(sc); s.setMinWidth(900); s.setMinHeight(520); s.show();
        host.widthProperty().addListener(o -> render()); host.heightProperty().addListener(o -> render()); render();
    }
    void show(String sc) { screen = sc; render(); }
    /** App screens scale by WIDTH so the design always fills the window edge to edge (height adapts and scrolls).
     *  Login keeps its 1280x800 proportions centred. */
    void render() {
        double W = host.getWidth(), H = host.getHeight(); if (W <= 0 || H <= 0) return;
        boolean lg = screen.equals("login");
        double dw = lg ? 1280 : DW, k = lg ? Math.min(W / 1280, H / 800) : W / DW; dh = lg ? 800 : H / k;
        Pane d = lg ? login() : app();
        d.setPrefSize(dw, dh); d.setMinSize(dw, dh); d.setMaxSize(dw, dh);
        d.setLayoutX((W - dw * k) / 2); d.setLayoutY((H - dh * k) / 2);
        d.getTransforms().setAll(new Scale(k, k, 0, 0));
        Pane root = new Pane(d); root.setPrefSize(W, H); root.setMaxSize(W, H); host.getChildren().setAll(root);
    }

    // ---------- helpers ----------
    static Image img(String n) { return new Image(Main.class.getResourceAsStream("/a/" + n + ".png")); }
    static ImageView iv(String n, double x, double y) { ImageView v = new ImageView(img(n)); v.setLayoutX(x); v.setLayoutY(y); return v; }
    /** places an asset so that its visible "core" starts exactly at (x,y), as in the prototype */
    static final Map<String, double[]> OFF = new HashMap<>();
    static {
        double[][] d = {{10,13.9},{11.3,14.1},{14,14},{13.9,13.9},{4,4},{4,4},{4,4},{4.3,4.3},{4,4},{4.1,4.1},{4.2,4.2},{4.1,4.1},{9.9,9.9},{10.1,10.1},{9.7,9.7},{.3,.3},{.4,.4},{.3,.3},{.2,.2},{.3,.3},{1.2,1.2}};
        String[] n = {"nav_dash_on","nav_grades_on","nav_dash_off","nav_grades_off","ic_medal","ic_book","ic_shield","ic_clock","ic_x","b_pass","b_pend","b_fail","big_pass","big_pend","big_fail","ic_logout","ic_plus","ic_close","ic_search","ic_filter","avatar"};
        for (int i = 0; i < n.length; i++) OFF.put(n[i], d[i]);
    }
    static ImageView at(String n, double x, double y) { double[] o = OFF.getOrDefault(n, new double[]{0, 0}); return iv(n, x - o[0], y - o[1]); }
    static Text text(String s, double x, double baseline, double size, FontWeight w, Color c) {
        Text t = new Text(s); t.setFont(Font.font("Inter", w, size)); t.setFill(c); t.setX(x); t.setY(baseline); return t;
    }
    static Text ctext(String s, double cx, double baseline, double size, FontWeight w, Color c) {
        Text t = text(s, 0, baseline, size, w, c); t.setX(cx - t.getLayoutBounds().getWidth() / 2); return t;
    }
    static Rectangle rr(double x, double y, double w, double h, double r, Paint fill, Paint stroke) {
        Rectangle e = new Rectangle(x, y, w, h); e.setArcWidth(r * 2); e.setArcHeight(r * 2); e.setFill(fill); e.setStroke(stroke); return e;
    }
    static Button hit(double x, double y, double w, double h, Runnable r) {
        Button b = new Button(); b.setLayoutX(x); b.setLayoutY(y); b.setPrefSize(w, h); b.setStyle("-fx-background-color:transparent;-fx-cursor:hand;"); b.setOnAction(e -> r.run()); return b;
    }
    static Polygon arrow(double cx, double cy, boolean down, double size, Color c) {
        Polygon p = down ? new Polygon(cx - size, cy - size * .6, cx + size, cy - size * .6, cx, cy + size * .7)
                         : new Polygon(cx - size * .6, cy - size, cx - size * .6, cy + size, cx + size * .7, cy); p.setFill(c); return p;
    }

    // ---------- grading logic ----------
    static String gwa(Integer g) {
        if (g == null) return "-.--";
        if (g >= 95) return "1.00"; if (g >= 92) return "1.25"; if (g >= 90) return "1.50"; if (g >= 88) return "1.75";
        if (g >= 85) return "2.00"; if (g >= 82) return "2.25"; if (g >= 79) return "2.50"; if (g >= 76) return "2.75";
        return g >= 75 ? "3.00" : "5.00";
    }
    static String remark(Integer g) { return g == null ? "PENDING" : g >= 75 ? "PASSED" : "FAILED"; }
    static double avg(List<Integer> gs) { return gs.stream().filter(Objects::nonNull).mapToInt(i -> i).average().orElse(Double.NaN); }
    static String f2(double d) { return Double.isNaN(d) ? "--.--" : String.format("%.2f", d); }
    static double gwaAvg(List<Subj> l) { return l.stream().map(Subj::overall).filter(Objects::nonNull).mapToDouble(g -> Double.parseDouble(gwa(g))).average().orElse(Double.NaN); }
    static String status(List<Subj> l) {
        long g = l.stream().filter(s -> s.overall() != null).count();
        if (g < l.size()) return "PENDING"; return l.stream().allMatch(s -> s.overall() >= 75) ? "PASSED" : "FAILED";
    }

    // Overall result only exists once BOTH semesters are fully graded; it is the equal-weight mean of the two semesters.
    static boolean complete(List<Subj> l) { return l.stream().allMatch(s -> s.overall() != null); }
    static boolean bothDone() { return complete(SEM.get(0)) && complete(SEM.get(1)); }
    static double overallAvg() { return !bothDone() ? Double.NaN : (avg(SEM.get(0).stream().map(Subj::overall).toList()) + avg(SEM.get(1).stream().map(Subj::overall).toList())) / 2; }
    static double overallGwa() { return !bothDone() ? Double.NaN : (gwaAvg(SEM.get(0)) + gwaAvg(SEM.get(1))) / 2; }

    // ================= LOGIN =================
    boolean pwVisible, emBad, pwBad; String emVal = "", pwVal = "";
    Pane login() {
        Pane p = new Pane(); p.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 60% 60%,#000000 0%,#071a38 40%,#081a37 100%);");
        ImageView bg = iv("bglogo", -60, -50); bg.setOpacity(.55); bg.setEffect(new GaussianBlur(2)); p.getChildren().add(bg);
        Rectangle card = rr(103, 72, 495, 655, 60, Color.rgb(255, 255, 255, .08), Color.rgb(255, 255, 255, .6)); card.setStrokeWidth(3);
        card.setEffect(new DropShadow(30, Color.rgb(80, 140, 255, .35)));
        p.getChildren().addAll(card, ctext("Login", 350, 152, 54, FontWeight.BOLD, Color.WHITE),
            ctext("Please sign in to access grade", 350, 228, 30, FontWeight.NORMAL, Color.web("#e4ecf6")), ctext("management", 350, 273, 30, FontWeight.NORMAL, Color.web("#e4ecf6")),
            text("Email", 137, 340, 28, FontWeight.NORMAL, Color.WHITE), text("Password", 137, 463, 28, FontWeight.NORMAL, Color.WHITE));
        Rectangle ef = rr(134, 363, 435, 54, 6, emBad ? Color.WHITE : Color.WHITE, emBad ? Color.RED : Color.web("#222")); Rectangle pf = rr(134, 494, 428, 54, 6, Color.WHITE, pwBad ? Color.RED : Color.web("#222"));
        TextField em = new TextField(emVal); em.setLayoutX(190); em.setLayoutY(366); em.setPrefSize(370, 48);
        PasswordField pw = new PasswordField(); pw.setText(pwVal); pw.setLayoutX(190); pw.setLayoutY(497); pw.setPrefSize(310, 48);
        TextField pws = new TextField(pwVal); pws.setLayoutX(190); pws.setLayoutY(497); pws.setPrefSize(310, 48);
        pws.textProperty().bindBidirectional(pw.textProperty()); pws.setVisible(pwVisible); pw.setVisible(!pwVisible);
        String fs = "-fx-background-color:transparent;-fx-font-size:21px;-fx-font-family:'Inter';-fx-text-fill:#222;-fx-prompt-text-fill:#777;-fx-background-insets:0;-fx-focus-color:transparent;-fx-faint-focus-color:transparent;";
        String bad = fs.replace("#222", "#ff0000").replace("#777", "#ff0000");
        em.setPromptText(emBad ? "Email is invalid" : "Email"); pw.setPromptText(pwBad ? "Password is invalid" : "Password"); pws.setPromptText(pw.getPromptText());
        em.setStyle(emBad ? bad : fs); pw.setStyle(pwBad ? bad : fs); pws.setStyle(pwBad ? bad : fs);
        ImageView ui = iv(emBad ? "u_red" : "u_gray", 144, 371), li = iv(pwBad ? "l_red" : "l_gray", 147, 501), eye = iv(pwVisible ? "eye_off" : "eye", 512, 513);
        Button eb = hit(505, 500, 50, 40, () -> { emVal = em.getText(); pwVal = pw.getText(); pwVisible = !pwVisible; show("login"); });
        Rectangle btn = rr(248, 604, 207, 54, 6, Color.web("#0066cc"), Color.web("#1f8fff")); btn.setEffect(new DropShadow(22, Color.rgb(60, 140, 255, .8)));
        Runnable submit = () -> {
            String e = em.getText().trim(), pwd = pw.getText();
            boolean eo = e.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$"), po = !pwd.isEmpty(), ok = eo && po && e.equalsIgnoreCase(EMAIL) && pwd.equals(PASS);
            if (ok) { emBad = pwBad = false; emVal = pwVal = ""; page = "dash"; show("app"); return; }
            emBad = !eo || po; pwBad = !po || eo; emVal = emBad ? "" : e; pwVal = ""; show("login");
        };
        Button lb = hit(248, 604, 207, 54, submit); pw.setOnAction(a -> submit.run()); em.setOnAction(a -> submit.run()); pws.setOnAction(a -> submit.run());
        p.getChildren().addAll(ef, pf, ui, li, em, pw, pws, eye, eb, btn, ctext("Login", 351, 640, 28, FontWeight.BOLD, Color.WHITE), lb);
        ImageView logo = iv("logo", 777, 114); logo.setFitWidth(320); logo.setFitHeight(320); logo.setEffect(new DropShadow(30, Color.rgb(0, 120, 255, .5)));
        p.getChildren().addAll(logo, ctext("NXP Santic", 937, 506, 44, FontWeight.BOLD, Color.WHITE), ctext("Student Grade Management", 937, 560, 29, FontWeight.NORMAL, Color.WHITE));
        return p;
    }

    // ================= APP SHELL =================
    Pane app() {
        Pane p = new Pane(); p.setStyle("-fx-background-color:linear-gradient(to bottom,#021a33,#00223b);"); p.getChildren().addAll(content(), sidebar());
        if (popup.equals("profile")) p.getChildren().add(profileModal()); return p;
    }
    void refresh() { show("app"); }
    Node sidebar() {
        Pane s = new Pane(); Rectangle bg = new Rectangle(256, dh); bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#031729")), new Stop(1, Color.web("#021120")))); s.getChildren().add(bg);
        ImageView av = at("avatar", 14, 54); av.setFitWidth(44); av.setFitHeight(44); s.getChildren().addAll(av, text("Hello, Mario", 69, 77, 26, FontWeight.BOLD, Color.WHITE), rr(69, 85, 127, 17, 8.5, Color.web("#00447f"), null), ctext("Student Dashboard", 132, 98, 11, FontWeight.BOLD, Color.web("#00d0ff")));
        boolean dash = page.equals("dash"), gr = page.equals("grades");
        s.getChildren().addAll(dash ? at("nav_dash_on", 25, 154) : at("nav_dash_off", 41, 165), hit(25, 154, 203, 41, () -> { page = "dash"; popup = ""; refresh(); }));
        // Grades: pill 25..228 (same slot), arrow at x=204
        s.getChildren().addAll(gr ? at("nav_grades_on", 25, 214) : at("nav_grades_off", 41, 224), arrow(204, 235, gradesOpen, 6, Color.web("#dbe7f3")), hit(25, 214, 203, 41, () -> { gradesOpen = !gradesOpen; if (gradesOpen) { page = "grades"; subOpen = sem == 2 ? 0 : sem; } popup = ""; refresh(); }));
        if (gradesOpen) {
            double y = 286;
            for (int k = 0; k < 2; k++) {
                boolean open = subOpen == k; final int kk = k;
                s.getChildren().addAll(ctext(SEM_NAMES[k], 150, y + 5, 14, FontWeight.BOLD, Color.web("#dbe7f3")), arrow(235, y, open, 6, Color.web("#dbe7f3")), hit(55, y - 14, 200, 28, () -> { subOpen = open ? -1 : kk; refresh(); }));
                y += 38;
                if (open) for (String per : new String[]{"Midterm", "Finals"}) {
                    boolean on = gr && sem == k && period.equals(per);
                    if (on) s.getChildren().add(rr(107, y - 14, 95, 27, 5, Color.web("#0066cc"), null));
                    s.getChildren().addAll(ctext(per, 155, y + 5, 13, FontWeight.BOLD, on ? Color.WHITE : Color.web("#dbe7f3")), hit(100, y - 14, 110, 28, () -> { sem = kk; period = per; page = "grades"; search = ""; filter = ""; popup = ""; refresh(); }));
                    y += 38;
                }
            }
        }
        s.getChildren().addAll(at("ic_logout", 42, dh - 70), text("Logout", 80, dh - 51, 14, FontWeight.BOLD, Color.web("#c3d3e2")), hit(36, dh - 75, 120, 36, () -> { page = "dash"; sem = 0; gradesOpen = false; popup = ""; search = filter = ""; show("login"); }));
        return s;
    }
    ScrollPane content() {
        Pane c = new Pane(); c.setStyle("-fx-background-color:transparent;");
        double h = page.equals("dash") ? dashboard(c) : grades(c);
        c.setPrefSize(DW - 256, Math.max(dh, h + 40)); ScrollPane sp = new ScrollPane(c); sp.setLayoutX(256); sp.setPrefSize(DW - 256, dh); sp.setMaxSize(DW - 256, dh);
        sp.setFitToWidth(true); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;"); sp.setVvalue(scroll); sp.vvalueProperty().addListener((o, a, b) -> scroll = b.doubleValue()); return sp;
    }
    // x coordinates below are page x minus 256
    void header(Pane c, String title, double ty, String sub, boolean picker) {
        c.getChildren().add(text(title, 67, ty + 13, 40, FontWeight.BOLD, Color.WHITE));
        Text t = text(sub, 69, ty + 56, 17, FontWeight.BOLD, DIM); c.getChildren().add(t);
        if (picker) { double ax = 69 + t.getLayoutBounds().getWidth() + 22; c.getChildren().addAll(arrow(ax, ty + 50, popup.equals("sem") ? false : true, 8, Color.web("#c9d6e2")), hit(69, ty + 36, t.getLayoutBounds().getWidth() + 50, 30, () -> { popup = popup.equals("sem") ? "" : "sem"; refresh(); }));
            if (popup.equals("sem")) { String[] it = {"First Semester", "Second Semester", "Overall Average"};
                for (int i = 0; i < 3; i++) { final int k = i; Pane row = new Pane(); row.setLayoutX(ax - 30); row.setLayoutY(ty + 70 + i * 32); row.setPrefSize(183, 32); row.setStyle("-fx-background-color:#031d36;-fx-border-color:#41688a;-fx-border-width:1;");
                    row.getChildren().add(text(it[i], 9, 21, 13, FontWeight.BOLD, Color.web("#c9d6e2"))); Button b = hit(0, 0, 183, 32, () -> { sem = k; search = filter = ""; popup = ""; scroll = 0; refresh(); }); row.getChildren().add(b); late.add(row); } } }
        Pane card = new Pane(); card.setLayoutX(714 + DX); card.setLayoutY(ty - 34); card.setPrefSize(285, 91);
        ImageView pav = at("avatar", 21, 12); pav.setFitWidth(68); pav.setFitHeight(68);
        card.getChildren().addAll(rr(0, 0, 285, 91, 8, Color.web("#00223b"), Color.web("#143253")), pav, text("Mario A. Mimay", 100, 31, 20, FontWeight.BOLD, Color.WHITE), text("Student ID: 26-1071", 100, 52, 12, FontWeight.BOLD, Color.web("#a7bacb")), text("BSIT - First Year - Block B", 100, 72, 12, FontWeight.BOLD, Color.web("#00d0ff")));
        card.setOnMouseClicked(e -> { popup = "profile"; refresh(); }); card.setCursor(Cursor.HAND); c.getChildren().add(card);
    }
    void statCard(Pane c, double x, double y, double w, double h, String icon, String l1, String l2, Node big, String f1, String f2) {
        c.getChildren().addAll(rr(x, y, w, h, 12, Color.web("#00223b"), Color.web("#143253")), at(icon, x + 26, y + 12));
        c.getChildren().add(text(l1, x + 93, y + 31, 17, FontWeight.BOLD, TXT)); if (!l2.isEmpty()) c.getChildren().add(text(l2, x + 93, y + 53, 17, FontWeight.BOLD, TXT));
        c.getChildren().add(big); c.getChildren().add(text(f1, x + 27, y + 142, 13, FontWeight.BOLD, DIM)); if (!f2.isEmpty()) c.getChildren().add(text(f2, x + 27, y + 160, 13, FontWeight.BOLD, DIM));
    }
    double dashboard(Pane c) {
        List<Subj> all = new ArrayList<>(); SEM.forEach(all::addAll);
        List<Subj> l = sem == 2 ? all : SEM.get(sem); String nm = sem == 2 ? "Overall Average" : SEM_NAMES[sem];
        header(c, "Student Grades Overview", 85, "School Year 2026-2027 \u2022 Read-Only Access \u2022 " + nm, true);
        long gr = l.stream().filter(s -> s.overall() != null).count(); String st = status(l); double a = sem == 2 ? overallAvg() : avg(l.stream().map(Subj::overall).toList());
        boolean pend = st.equals("PENDING");
        Text big1 = text(f2(a), 96, 300, 45, FontWeight.BOLD, Color.WHITE); big1.setX(96);
        if (sem == 2) statCard(c, 69, 183, 390, 178, "ic_medal", "Overalll Average", "", big1, "Auto-computed across " + l.size(), "subjects");
        else statCard(c, 69, 183, 390, 178, "ic_medal", "General Average for", nm, big1, "Auto-computed across " + l.size(), "subjects");
        statCard(c, 499, 180, 390, 178, st.equals("PASSED") ? "ic_shield" : st.equals("FAILED") ? "ic_x" : "ic_clock", "Overall Academic", "Status", new Group(), st.equals("FAILED") ? "One or more subjects below" : "All subjects meet passing", st.equals("FAILED") ? "passing criteria" : "criteria");
        c.getChildren().add(at("big_" + (pend ? "pend" : st.equals("PASSED") ? "pass" : "fail"), 524, 257));
        statCard(c, 929, 186, 390, 178, "ic_book", "Enrolled Subjects", "", text(l.size() + " Subjects", 955, 300, 42, FontWeight.BOLD, Color.WHITE), gr + " Graded   \u2022 " + (l.size() - gr) + " Pending", "");
        double r = sem == 2 ? overallCard(c, all, a) : tableCard(c, "Subject Grade Breakdown", l, true, 400, a);
        c.getChildren().addAll(late); late.clear(); return r;
    }
    double grades(Pane c) {
        int k = sem == 2 ? 0 : sem; sem = k;
        header(c, "Student Grades", 85, "School Year 2026-2027 \u2022 Read-Only Access \u2022 " + SEM_NAMES[k], false);
        double r = tableCard(c, period, SEM.get(k), false, 163, 0);
        c.getChildren().addAll(late); late.clear(); return r;
    }

    // ---------- tables ----------
    double tableCard(Pane c, String title, List<Subj> src, boolean overall, double top, double a) {
        Function<Subj, Integer> gv = s -> overall ? s.overall() : period.equals("Midterm") ? s.mid() : s.fin();
        List<Subj> rows = new ArrayList<>(src); rows.removeIf(s -> !(s.code() + " " + s.title()).toLowerCase().contains(search.toLowerCase()));
        switch (filter) {
            case "Passed" -> rows.removeIf(s -> !"PASSED".equals(remark(gv.apply(s))));
            case "Failed" -> rows.removeIf(s -> !"FAILED".equals(remark(gv.apply(s))));
            case "Pending" -> rows.removeIf(s -> gv.apply(s) != null);
            case "Highest to Lowest" -> rows.sort(Comparator.comparing((Subj s) -> gv.apply(s) == null ? -1 : gv.apply(s)).reversed());
            case "Lowest to Highest" -> rows.sort(Comparator.comparing((Subj s) -> gv.apply(s) == null ? 999 : gv.apply(s)));
            default -> {}
        }
        double rowsH = Math.max(1, rows.size()) * 61, h = 123 + rowsH + (overall ? 90 : 0);
        c.getChildren().add(rr(67, top, 932 + DX, h, 8, Color.web("#00223b"), Color.web("#143253")));
        c.getChildren().add(text(title, 96, top + 33, 24, FontWeight.BOLD, Color.WHITE));
        searchField = new TextField(search); searchField.setPromptText("Search Subject"); searchField.setLayoutX(560 + DX); searchField.setLayoutY(top + 13); searchField.setPrefSize(372, 30);
        searchField.setStyle("-fx-background-color:#00203a;-fx-background-radius:15;-fx-border-color:#2f5577;-fx-border-radius:15;-fx-text-fill:white;-fx-prompt-text-fill:#9db3c4;-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:0 0 0 40;-fx-font-family:'Inter';");
        searchField.setOnKeyReleased(e -> { search = searchField.getText(); refresh(); Platform.runLater(() -> { searchField.requestFocus(); searchField.end(); }); });
        c.getChildren().addAll(searchField, at("ic_search", 574 + DX, top + 20), at("ic_filter", 948 + DX, top + 19), hit(940 + DX, top + 12, 40, 32, () -> { popup = popup.equals("filter") ? "" : "filter"; refresh(); }));
        c.getChildren().add(new Line(67, top + 63, 999 + DX, top + 63) {{ setStroke(Color.web("#204065")); }});
        Rectangle hb = new Rectangle(68, top + 64, 930 + DX, 60); hb.setFill(Color.web("#031f3a")); c.getChildren().add(hb);
        double hy = top + 98; Color hc = Color.web("#9db3c4");
        c.getChildren().addAll(text("#", 96, hy - 2, 19, FontWeight.BOLD, hc), text("Subject Code", 169, hy, 14, FontWeight.BOLD, hc), text("Subject Title", 300, hy, 14, FontWeight.BOLD, hc));
        if (overall) c.getChildren().addAll(text("Overall Final Grade", 649 + DX, hy, 14, FontWeight.BOLD, hc), text("GWA", 805 + DX, hy, 14, FontWeight.BOLD, hc), text("Remarks", 899 + DX, hy, 14, FontWeight.BOLD, hc));
        else c.getChildren().addAll(text("Weighted Grade", 747 + DX, hy, 14, FontWeight.BOLD, hc), text("Remarks", 899 + DX, hy, 14, FontWeight.BOLD, hc));
        double y = top + 124; int i = 1;
        for (Subj s : rows) {
            Integer v = gv.apply(s); double cy = y + 36;
            c.getChildren().add(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }});
            c.getChildren().addAll(text("" + i, 96, cy, 17, FontWeight.BOLD, TXT), text(s.code(), 169, cy, 15, FontWeight.BOLD, TXT), text(s.title(), 300, cy, 15, FontWeight.BOLD, TXT));
            Color cy2 = Color.web("#00d0ff"); String vs = v == null ? "--" : "" + v;
            if (overall) c.getChildren().addAll(ctext(vs, 659 + DX, cy + 1, 15, FontWeight.BOLD, cy2), text(gwa(v), 807 + DX, cy, 15, FontWeight.BOLD, TXT));
            else c.getChildren().add(ctext(vs, 802 + DX, cy + 1, 15, FontWeight.BOLD, cy2));
            String r = remark(v); c.getChildren().add(at("b_" + (r.equals("PASSED") ? "pass" : r.equals("FAILED") ? "fail" : "pend"), 899 + DX, cy - 15));
            y += 61; i++;
        }
        if (rows.isEmpty()) c.getChildren().add(ctext("No subjects found", 533, y + 36, 15, FontWeight.BOLD, TXT));
        if (overall) {
            long g = src.stream().filter(s -> s.overall() != null).count(); y = top + 124 + Math.max(1, rows.size()) * 61;
            c.getChildren().add(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }});
            c.getChildren().add(text("SUMMARY", 96, y + 52, 14, FontWeight.BOLD, hc));
            sumCell(c, 354 * F, y, "General Average", f2(a)); sumCell(c, 587 * F, y, "Average Grade Point", Double.isNaN(gwaAvg(src)) ? "-.-" : f2(gwaAvg(src)));
            sumCell(c, 834 * F, y, "Subjects Count", g + " / " + src.size()); if (g == 0) c.getChildren().add(ctext("(Graded)", 834 * F, y + 82, 11, FontWeight.BOLD, hc));
        }
        if (popup.equals("filter")) filterMenu(c, top);
        return top + h;
    }
    void sumCell(Pane c, double cx, double y, String label, String v) { c.getChildren().addAll(ctext(label, cx, y + 24, 14, FontWeight.BOLD, Color.web("#9db3c4")), ctext(v, cx, y + 60, 34, FontWeight.BOLD, Color.WHITE)); }
    void filterMenu(Pane c, double top) {
        String[] it = {"Passed", "Failed", "Pending", "Highest to Lowest", "Lowest to Highest"};
        for (int i = 0; i < it.length; i++) { final String n = it[i]; Pane r = new Pane(); r.setLayoutX(824 + DX); r.setLayoutY(top + 46 + i * 33); r.setPrefSize(176, 33);
            r.setStyle("-fx-background-color:" + (filter.equals(n) ? "#0b3f73" : "#031d36") + ";-fx-border-color:#41688a;-fx-border-width:1;");
            r.getChildren().addAll(text(n, 8, 21, 12, FontWeight.BOLD, Color.web("#c9d6e2")), hit(0, 0, 176, 33, () -> { filter = filter.equals(n) ? "" : n; popup = ""; refresh(); })); c.getChildren().add(r); }
    }
    double overallCard(Pane c, List<Subj> all, double a) {
        double top = 400, h = 123 + 2 * 61 + 90;
        c.getChildren().add(rr(67, top, 932 + DX, h, 8, Color.web("#00223b"), Color.web("#143253")));
        c.getChildren().addAll(text("Overall Average", 96, top + 33, 24, FontWeight.BOLD, Color.WHITE), at("ic_filter", 948 + DX, top + 19), new Line(67, top + 63, 999 + DX, top + 63) {{ setStroke(Color.web("#204065")); }});
        Rectangle hb = new Rectangle(68, top + 64, 930 + DX, 60); hb.setFill(Color.web("#031f3a")); c.getChildren().add(hb);
        double hy = top + 98; Color hc = Color.web("#9db3c4");
        c.getChildren().addAll(text("#", 96, hy - 2, 19, FontWeight.BOLD, hc), text("Grading Period", 169, hy, 14, FontWeight.BOLD, hc), text("# of Subjects", 330, hy, 14, FontWeight.BOLD, hc),
            text("Overall Final Grade", 649 + DX, hy, 14, FontWeight.BOLD, hc), text("GWA", 805 + DX, hy, 14, FontWeight.BOLD, hc), text("Remarks", 899 + DX, hy, 14, FontWeight.BOLD, hc));
        double y = top + 124;
        for (int i = 0; i < 2; i++) { List<Subj> l = SEM.get(i); double cy = y + 36; double av = avg(l.stream().map(Subj::overall).toList()); String s = status(l);
            c.getChildren().addAll(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }}, text("" + (i + 1), 96, cy, 17, FontWeight.BOLD, TXT), text(SEM_NAMES[i], 169, cy, 15, FontWeight.BOLD, TXT), text("" + l.size(), 345, cy, 15, FontWeight.BOLD, TXT),
                ctext(Double.isNaN(av) ? "--" : f2(av), 659 + DX, cy + 1, 15, FontWeight.BOLD, Color.web("#00d0ff")), text(Double.isNaN(gwaAvg(l)) ? "-.--" : f2(gwaAvg(l)), 807 + DX, cy, 15, FontWeight.BOLD, TXT),
                at("b_" + (s.equals("PASSED") ? "pass" : s.equals("FAILED") ? "fail" : "pend"), 899 + DX, cy - 15)); y += 61; }
        c.getChildren().addAll(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }}, text("SUMMARY", 96, y + 52, 14, FontWeight.BOLD, hc));
        sumCell(c, 440 * F, y, "Overalll Average", f2(a)); sumCell(c, 700 * F, y, "GWA", Double.isNaN(overallGwa()) ? "-.--" : f2(overallGwa()));
        return top + h;
    }

    // ---------- profile modal ----------
    Node profileModal() {
        Pane m = new Pane(); m.setPrefSize(DW, dh); m.setStyle("-fx-background-color:rgba(0,0,0,.35);"); m.setOnMouseClicked(e -> {});
        m.getChildren().add(rr(522, 42, 555, 716, 36, Color.web("#0a2d52", .96), Color.web("#4a78a6", .7)));
        ImageView ph = at("avatar", 722, 118); ph.setFitWidth(130); ph.setFitHeight(130);
        m.getChildren().addAll(ctext("Student Profile", 800, 99, 40, FontWeight.BOLD, Color.WHITE), at("ic_close", 1012, 68), hit(1000, 58, 50, 44, () -> { popup = ""; refresh(); }),
            ph, at("ic_plus", 828, 215), ctext("Profile Photo", 788, 280, 15, FontWeight.BOLD, Color.web("#c3d3e2")));
        String[][] f = {{"Student's Name:", "Mario A. Mimay"}, {"Student's ID number", "26-1071"}, {"Student's Email", EMAIL}, {"Program", "Bachelor of Science in information technology"}, {"Year & Block", "1st Year - Block B"}};
        for (int i = 0; i < f.length; i++) { double y = 316 + i * 87;
            m.getChildren().addAll(text(f[i][0], 543, y, 17, FontWeight.BOLD, Color.web("#00d0ff")), rr(558, y + 12, 490, 42, 6, Color.web("#031e38"), Color.web("#2f5577")), text(f[i][1], 580, y + 40, 18, FontWeight.BOLD, Color.WHITE)); }
        for (Node n : m.getChildren()) n.setTranslateX((DW - 1280) / 2);   // centre in the wider layout
        return m;
    }
    public static void main(String[] a) { launch(a); }
}
