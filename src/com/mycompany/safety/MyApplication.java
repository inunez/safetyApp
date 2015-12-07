package com.mycompany.safety;

import com.codename1.capture.Capture;
import com.codename1.components.InfiniteProgress;
import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import com.codename1.io.Log;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.ui.Button;
import com.codename1.ui.ComboBox;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Font;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.table.TableLayout;
import com.codename1.ui.util.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyApplication {

    private Form current;
    private Resources theme;
    private Database db = null;
    //TO DO select the user id when the user logged in
    private String userID = "inunez";

    public void init(Object context) {
        try {
            theme = Resources.openLayered("/theme");
            UIManager.getInstance().setThemeProps(theme.getTheme(theme.getThemeResourceNames()[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enable the "Hamburger" menu
        Display.getInstance().setCommandBehavior(Display.COMMAND_BEHAVIOR_SIDE_NAVIGATION);

    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        try {
            loadDatabaseSQL();
        } catch (IOException ex) {
            //Logger.getLogger(MyApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        Form main = createMainForm();
        main.show();
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }

    public void destroy() {
    }

    public Form createMainForm() {
        Form main = new Form("Safety App");
        main.setLayout(new BorderLayout());
        addMenu(main);

        try {
            Image im = Image.createImage("/Logo.jpg").modifyAlpha((byte) 128);
            im = im.scaledWidth(Display.getInstance().getDisplayWidth());
            Label logo = new Label(im);
            main.addComponent(BorderLayout.CENTER, logo);

        } catch (IOException ex) {
            Log.p("Error Main Form");
        }
        TextArea desc = new TextArea();
        desc.setText("Safetapp 2015");
        desc.setEditable(false);

        main.addComponent(BorderLayout.SOUTH, desc);
        return main;
    }

    /**
     * This method builds a UI Entry dynamically from a data Map object.
     */
    private Component createEntry(Map metaData, List data, int index) {
        String dataItem = "";

        final Container cnt = new Container(new BorderLayout());
        cnt.setUIID("MultiButton");
        
        Label icon = createIconLabel("IN");

        cnt.addComponent(BorderLayout.WEST, icon);

        Container center = new Container(new BorderLayout());

        dataItem = (String) data.get((int) metaData.get("title"));
//        Label title = new Label(dataItem + " - " + dtFormat.format(dateValue));
        Label title = new Label(dataItem);
        title.setUIID("MultiLine1");
        center.addComponent(BorderLayout.NORTH, title);
        
        final Container south = new Container(new BorderLayout());
        
        dataItem = (String) data.get((int) metaData.get("date"));
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        Date dateValue = null;
        try {
            dateValue = dt.parse(dataItem);
        } catch (ParseException ex) {
        }
        SimpleDateFormat dtFormat = new SimpleDateFormat("dd MMM yy");
        Label date = new Label(dtFormat.format(dateValue));
        date.setUIID("MultiLine3");
        south.addComponent(BorderLayout.EAST, date);
        
        dataItem = (String) data.get((int) metaData.get("subTitle"));
        Label subTitle = new Label(dataItem);
        subTitle.setUIID("MultiLine3");
        south.addComponent(BorderLayout.WEST, subTitle);
        
        center.addComponent(BorderLayout.SOUTH, south);
        cnt.addComponent(BorderLayout.CENTER, center);
        //cnt.addComponent(BorderLayout.SOUTH, south);
        
        return cnt;
    }

    private Label createIconLabel(String text) {
        int w = 100;
        int h = 100;
        Image originalImage = Image.createImage(w, h);
        
        //Image originalImage = FontImage.create(text, UIManager.getInstance().getComponentStyle("IconFont"));

        Graphics graphic = originalImage.getGraphics();
        
        int charWidth = graphic.getFont().stringWidth(text);
        int charHeight = graphic.getFont().getHeight();
        
        graphic.setColor(0xCCCCCC);
        
        Font myFont;
//        if(Font.isTrueTypeFileSupported()) { 
//            myFont = Font.createTrueTypeFont(fontName, fontFileName); 
//            myFont = myFont.derive(sizeInPixels, Font.STYLE_PLAIN); 
//            // do something with the font 
//        }else 
        myFont = Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
//            myFont = UIManager.getInstance().getComponentStyle("IconFont").getFont();
        
        graphic.setFont(myFont);
        graphic.drawString(text, (int) (w-charWidth)/2, (int) (h-charHeight)/2);

        Image maskImage = Image.createImage(w, h);
        Graphics g = maskImage.getGraphics();
        g.setAntiAliased(true);
        g.setColor(0x000000);
        g.fillRect(0, 0, w, h);
        g.setColor(0xff00ff);
        g.fillArc(0, 0, w, h, 0, 360);

        Object mask = maskImage.createMask();

        Image maskedImage = originalImage.applyMask(mask);
        return new Label(maskedImage);
    }

    private void addWaitingProgress(Form f) {
        addWaitingProgress(f, true, f.getContentPane());
    }

    private void addWaitingProgress(Form f, boolean center, Container pane) {
        pane.setVisible(false);
        Container cnt = f.getLayeredPane();
        BorderLayout bl = new BorderLayout();
        bl.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE);
        cnt.setLayout(bl);
        if (center) {
            cnt.addComponent(BorderLayout.CENTER, new InfiniteProgress());
        } else {
            Container top = new Container();
            BorderLayout bl1 = new BorderLayout();
            bl1.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE);
            top.setLayout(bl1);
            top.addComponent(BorderLayout.CENTER, new InfiniteProgress());

            cnt.addComponent(BorderLayout.NORTH, top);
        }
    }

    private void removeWaitingProgress(Form f) {
        removeWaitingProgress(f, f.getContentPane());
    }

    private void removeWaitingProgress(Form f, Container pane) {
        Container cnt = f.getLayeredPane();
        cnt.removeAll();
        pane.setVisible(true);
    }

    private void updateScreenFromNetwork(final Form f, final String table, String filter, String fields, Map metaData) {
        //show a waiting progress on the Form
        addWaitingProgress(f);

        //run the networking on a background thread
        Display.getInstance().scheduleBackgroundTask(new Runnable() {

            public void run() {
                //TO DO create access to web service to request data and update local database
//                final List entries = ServerAccess.getEntriesFromServer(table, filter);

                final Map entries = getResultSet(table, false, filter, fields);
                //build the UI entries on the EDT using the callSerially
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        Container cnt = f.getContentPane();
                        for (int i = 0; i < entries.size(); i++) {
                            List data = (List) entries.get(i);
                            cnt.addComponent(createEntry(metaData, data, i));
                        }
                        f.revalidate();
                        //remove the waiting progress from the Form
                        removeWaitingProgress(f);
                    }
                });

            }
        });

    }

    private void addMenu(Form f) {
        f.addCommand(new Command("Non Conformance") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                showNonConformance();
            }

        });
        f.addCommand(new Command("Safety Walk") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                showSafetyWalk();
            }

        });

        f.addCommand(new Command("Plant Register") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                showPlantRegister();
            }

        });

        f.addCommand(new Command("Reports") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                showReports();
            }

        });

        f.addCommand(new Command("Logout") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
//                    client.logout();
//                    showLoginForm();
                } catch (Exception ex) {
//                    showError(ex.getMessage());
                }
            }

        });
    }

    private void showNonConformance() {
        Form nonConformance = new Form("Non Conformance");

        nonConformance.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        nonConformance.setScrollableY(true);
//                final CustomToolbar bar = new CustomToolbar(true);
        Toolbar bar = new Toolbar();
        bar.setScrollOffUponContentPane(true);
//                nonConformance.getContentPane().addScrollListener(bar);
        nonConformance.setToolBar(bar);
        addMenu(nonConformance);

        //Table and indexes
        Map<String, Integer> metaData = new HashMap<>();
        metaData.put("title", 15);
        metaData.put("subTitle", 16);
        metaData.put("date", 3);;
        String table = " non_conformance nc INNER JOIN user u on nc.user_id = u.user_id "
                + "INNER JOIN company c ON nc.company_id = c.company_id "
                + "INNER JOIN location l ON nc.location_id = l.location_id";
        String filter = "nc.user_id = '" + userID + "'";
        String fields = "nc.*, u.user_name, u.user_surname, c.company_name, l.location_description";

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(nonConformance, table, filter, fields, metaData);
                        nonConformance.revalidate();
                    }
                });
            }
        });

        bar.addCommandToOverflowMenu(new Command("New Report ") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Form report = createReportForm(nonConformance);
                report.show();
            }
        });

        bar.addCommandToOverflowMenu(new Command("Remove ") {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }

        });

        nonConformance.show();

        updateScreenFromNetwork(nonConformance, table, filter, fields, metaData);

    }

    private void showSafetyWalk() {
        Form safetyWalk = new Form("Safety Walk");

        safetyWalk.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        safetyWalk.setScrollableY(true);
//                final CustomToolbar bar = new CustomToolbar(true);
        Toolbar bar = new Toolbar();
        bar.setScrollOffUponContentPane(true);
//                safetyWalk.getContentPane().addScrollListener(bar);
        safetyWalk.setToolBar(bar);
        addMenu(safetyWalk);

        //Table and indexes
        Map<String, Integer> metaData = new HashMap<>();
        metaData.put("title", 15);
        metaData.put("subTitle", 16);
        metaData.put("date", 3);
        String table = "safety_walk";
        String filter = "\"user_id = '\"+ userID + \"'\"";
        String fields = "";

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(safetyWalk, table, filter, fields, metaData);
                        safetyWalk.revalidate();
                    }
                });
            }
        });

        bar.addCommandToOverflowMenu(new Command("New Report ") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Form report = createReportForm(safetyWalk);
                report.show();
            }
        });

        bar.addCommandToOverflowMenu(new Command("Remove ") {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }

        });

        safetyWalk.show();

        updateScreenFromNetwork(safetyWalk, table, filter, fields, metaData);

    }

    private void showPlantRegister() {
        Form plantRegister = new Form("Plant Register");

        plantRegister.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        plantRegister.setScrollableY(true);
//                final CustomToolbar bar = new CustomToolbar(true);
        Toolbar bar = new Toolbar();
        bar.setScrollOffUponContentPane(true);
//                plantRegister.getContentPane().addScrollListener(bar);
        plantRegister.setToolBar(bar);
        addMenu(plantRegister);

        //Table and indexes
        Map<String, Integer> metaData = new HashMap<>();
        metaData.put("title", 0);
        metaData.put("subTitle", 1);
        metaData.put("date", 2);
        String table = "plant_registration";
        String filter = "\"user_id = '\"+ userID + \"'\"";
        String fields = "";

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(plantRegister, table, filter, fields, metaData);
                        plantRegister.revalidate();
                    }
                });
            }
        });

        bar.addCommandToOverflowMenu(new Command("New Report ") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Form report = createReportForm(plantRegister);
                report.show();
            }
        });

        bar.addCommandToOverflowMenu(new Command("Remove ") {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }

        });

        plantRegister.show();

        updateScreenFromNetwork(plantRegister, table, filter, fields, metaData);

    }

    private void showReports() {
        Form reports = new Form("Reports");

        reports.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        reports.setScrollableY(true);
//                final CustomToolbar bar = new CustomToolbar(true);
        Toolbar bar = new Toolbar();
        bar.setScrollOffUponContentPane(true);
//                reports.getContentPane().addScrollListener(bar);
        reports.setToolBar(bar);
        addMenu(reports);

        //Table and indexes
        Map<String, Integer> metaData = new HashMap<>();
        metaData.put("title", 0);
        metaData.put("subTitle", 1);
        metaData.put("date", 2);
        String table = "reports";
        String filter = "\"user_id = '\"+ userID + \"'\"";
        String fields = "";

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(reports, table, filter, fields, metaData);
                        reports.revalidate();
                    }
                });
            }
        });

        reports.show();

        updateScreenFromNetwork(reports, table, filter, fields, metaData);

    }

    public Form createReportForm(Form back) {
        Form report = new Form("Non-Conformance Report");

        Image im = null;
        Label logo;
        try {
            im = Image.createImage("/Logo.jpg").modifyAlpha((byte) 16);
        } catch (IOException ex) {
            //Logger.getLogger(MyApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (im != null) {
            im = im.scaledWidth(Display.getInstance().getDisplayWidth());
            logo = new Label(im);
//            Hashtable h = new Hashtable();
//            h.put("bgImage", im);
//            UIManager.getInstance().setThemeProps(h);
//            Display.getInstance().getCurrent().refreshTheme();
        } else {
            logo = new Label("No Logo");
        }

        report.setLayout(new BorderLayout());
        Toolbar bar = new Toolbar();
        report.setToolBar(bar);
        addMenu(report);

        bar.addCommandToOverflowMenu(new Command("Back ") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                back.showBack();
            }
        });

        //TOP
        TableLayout topLayout = new TableLayout(2, 3);
        Container top = new Container(topLayout);
        Label lblReporter = new Label("Reported by ");
        TableLayout.Constraint constraint = topLayout.createConstraint();
        constraint.setWidthPercentage(30);
        top.addComponent(constraint, lblReporter);

        TextArea txtReporterName = new TextArea();
        txtReporterName.setEditable(false);
        txtReporterName.setHint("Name");
        constraint = topLayout.createConstraint();
        constraint.setWidthPercentage(45);
        top.addComponent(constraint, txtReporterName);

        constraint = topLayout.createConstraint();
        constraint.setWidthPercentage(25);
        constraint.setHeightPercentage(5);
        constraint.setVerticalSpan(2);
        top.addComponent(constraint, logo);

        Label lblSignature = new Label("Signature");
        constraint = topLayout.createConstraint();
        constraint.setWidthPercentage(30);
        top.addComponent(constraint, lblSignature);

        TextArea txtReporterSignature = new TextArea();
        txtReporterSignature.setEditable(false);
        txtReporterSignature.setHint("Signature");
        constraint = topLayout.createConstraint();
        constraint.setWidthPercentage(45);
        top.addComponent(constraint, txtReporterSignature);

        //MIDDLE
        TableLayout middleLayout = new TableLayout(13, 2);
        Container middle = new Container(middleLayout);

        middle.addComponent(new Label("Date"));
        TextField txtDate = new TextField();
        txtDate.setHint("DD/MM/YYYY");
        constraint = middleLayout.createConstraint();
        constraint.setWidthPercentage(50);
        middle.addComponent(constraint, txtDate);

        middle.addComponent(new Label("Project"));
        //TO DO replace 6 by company ID of user
        Map resultset = getResultSet("project", "project_company = 6");
        String[] comboData = getArrayStringFromResultset(resultset, 1);
        ComboBox combo = new ComboBox(new DefaultListModel(comboData));
        middle.addComponent(combo);

        middle.addComponent(new Label("Company"));
        resultset = getResultSet("company");
        comboData = getArrayStringFromResultset(resultset, 2);
        combo = new ComboBox(new DefaultListModel(comboData));
        middle.addComponent(combo);

        middle.addComponent(new Label("Attention to"));
        TextField txtAttentionTo = new TextField();
        middle.addComponent(txtAttentionTo);

        middle.addComponent(new Label("Location"));
        TextField txtLocation = new TextField();
        middle.addComponent(txtLocation);

        middle.addComponent(new Label("Type of NC"));
        String[] t = {"Type A1", "Type B1", "Type C1"};
        combo = new ComboBox(new DefaultListModel(t));
        middle.addComponent(combo);

        middle.addComponent(new Label("Risk Ranking"));
        String[] r = {"Extreme Risk - Immediate action required", "Very High Risk - Immediate action required", "High Risk - Immediate action required",
            "Medium Risk - Close-Of-Business of current day", "Low Risk - Action within 24 Hours", "Very Low Risk - Action within 48 Hours"};
        combo = new ComboBox(new DefaultListModel(r));
        middle.addComponent(combo);

        Label lblDesc = new Label("Description");
        constraint = middleLayout.createConstraint();
        constraint.setHorizontalAlign(Component.CENTER);
        constraint.setHorizontalSpan(2);
        middle.addComponent(constraint, lblDesc);

        TextArea desc = new TextArea();
        constraint = middleLayout.createConstraint();
        constraint.setHorizontalSpan(2);
        constraint.setHeightPercentage(10);
        constraint.setWidthPercentage(100);
        middle.addComponent(constraint, desc);

        Label lblAction = new Label("Action Required");
        constraint = middleLayout.createConstraint();
        constraint.setHorizontalAlign(Component.CENTER);
        constraint.setHorizontalSpan(2);
        middle.addComponent(constraint, lblAction);

        TextArea action = new TextArea();
        constraint = middleLayout.createConstraint();
        constraint.setHorizontalSpan(2);
        constraint.setHeightPercentage(10);
        constraint.setWidthPercentage(100);
        middle.addComponent(constraint, action);

        Label lblImages = new Label("Images");
        constraint = middleLayout.createConstraint();
        constraint.setHorizontalAlign(Component.CENTER);
        constraint.setHorizontalSpan(2);
        middle.addComponent(constraint, lblImages);

        Label images = new Label();
        constraint = middleLayout.createConstraint();
        constraint.setHorizontalSpan(2);
        constraint.setHeightPercentage(10);
        constraint.setWidthPercentage(100);
        middle.addComponent(constraint, images);

        //BOTTOM
        Container bottom = new Container(new GridLayout(1, 3));
        Button btnCancel = new Button("Cancel");
        btnCancel.addActionListener((e) -> {
            back.showBack();
        });
        bottom.addComponent(btnCancel);

        Button btnImages = new Button("Add Image");
        btnImages.addActionListener((e) -> {
            addImages(images);
            report.revalidate();
        });
        bottom.addComponent(btnImages);

        Button btnSave = new Button("Save");
        btnSave.addActionListener((e) -> {
            //save();
        });
        bottom.addComponent(btnSave);

        report.addComponent(BorderLayout.NORTH, top);
        middle.setScrollableY(true);
        report.addComponent(BorderLayout.CENTER, middle);
        report.addComponent(BorderLayout.SOUTH, bottom);

        report.setScrollable(true);
        return report;
    }

    private void addImages(Label listImages) {

        Image i = listImages.getStyle().getBgImage();
        if (i != null) {
            i.dispose();
        }
        listImages.getStyle().setBgImage(null);

        Capture.capturePhoto((ActionEvent evt) -> {
            InputStream is = null;
            try {
                String path = (String) evt.getSource();
                System.out.println("path " + path);
                is = com.codename1.io.FileSystemStorage.getInstance().openInputStream(path);
                Image i1 = Image.createImage(is);
                listImages.setIcon(i1.scaledWidth(300));

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void loadDatabaseSQL() throws IOException {

        try {
            db = Database.openOrCreate("Safetapp.db");

//            DAOProvider provider = new DAOProvider(db, "/Safetapp.db", 2);
//            System.out.println(provider.toString());
//            DAO collectionDAO = (DAO) provider.get("non_conformance");
//
//            c = db.executeQuery("PRAGMA table_info(non_conformance)");
//            Map myFields = new HashMap();
//            assertBool(myFields.containsKey("name"), "Collections table should contain name field.");
//            assertBool(myFields.containsKey("id"), "Collections table should contain id field");
//            Map idField = (Map) myFields.get("id");
//            Map nameField = (Map) myFields.get("name");
        } catch (IOException e) {

        } finally {

        }
    }

    public Map getResultSet(String table) {
        return getResultSet(table, true, "", "");
    }

    public Map getResultSet(String table, String filter) {
        return getResultSet(table, true, filter, "");
    }

    public Map getResultSet(String table, boolean allFields, String filter, String fieldList) {
        Map<Integer, List> resultSet = new HashMap<>();
        Cursor c;
        String sql;
        if (allFields) {
            sql = "SELECT * FROM ";
        } else {
            sql = "SELECT " + fieldList + " FROM ";
        }
        sql = sql + table;
        if (filter != null && !"".equals(filter)) {
            sql = sql + " WHERE " + filter;
        }

        try {
            c = db.executeQuery(sql);

            while (c.next()) {
                List fields = new ArrayList();

                Row row = c.getRow();
                for (int i = 0; i < c.getColumnCount(); i++) {
                    String value = row.getString(i);
                    fields.add(value);
                }
                resultSet.put(c.getPosition() - 1, fields);
            }
            c.close();
        } catch (IOException ex) {
            System.out.println("Error " + ex.getMessage());
        }

        return resultSet;
    }

    private String[] getArrayStringFromResultset(Map resultset, int index) {
        String[] p = new String[resultset.size()];// =(String[]) resultset.values().toArray();

        int i = 0;
        for (Object array : resultset.values()) {
            p[i] = ((ArrayList) array).get(index).toString();
            i += 1;
        }
        return p;
    }
}
