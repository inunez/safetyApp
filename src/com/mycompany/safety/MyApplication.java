package com.mycompany.safety;

import com.codename1.components.InfiniteProgress;
import com.codename1.io.Log;
import com.codename1.media.Media;
import com.codename1.media.MediaManager;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.URLImage;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.util.Resources;
import java.io.IOException;
import java.util.List;
import java.util.Map;
        
public class MyApplication {

    private Form current;
    private Resources theme;

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
        desc.setText("Safaty App 2015");
        desc.setEditable(false);

        main.addComponent(BorderLayout.SOUTH, desc);
        return main;
    }

    /**
     * This method builds a UI Entry dynamically from a data Map object.
     */
    private Component createEntry(Map data) {
        final Container cnt = new Container(new BorderLayout());
        cnt.setUIID("MultiButton");
        Label icon = new Label();
        //take the time and use it as the identifier of the image
        String time = (String) data.get("date_taken");
        String link = (String) ((Map) data.get("media")).get("m");

        EncodedImage im = (EncodedImage) theme.getImage("flickr.png");
        icon.setIcon(URLImage.createToStorage(im, time, link, null));
        cnt.addComponent(BorderLayout.WEST, icon);

        Container center = new Container(new BorderLayout());

        Label des = new Label((String) data.get("title"));
        des.setUIID("MultiLine1");
        center.addComponent(BorderLayout.NORTH, des);
        Label author = new Label((String) data.get("author"));
        author.setUIID("MultiLine2");
        center.addComponent(BorderLayout.SOUTH, author);

        cnt.addComponent(BorderLayout.CENTER, center);
        return cnt;
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

    private void updateScreenFromNetwork(final Form f, final String tag) {
        //show a waiting progress on the Form
        addWaitingProgress(f);

        //run the networking on a background thread
        Display.getInstance().scheduleBackgroundTask(new Runnable() {

            public void run() {
                final List entries = ServerAccess.getEntriesFromFlickrService(tag);

                //build the UI entries on the EDT using the callSerially
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        Container cnt = f.getContentPane();
                        for (int i = 0; i < entries.size(); i++) {
                            Map data = (Map) entries.get(i);
                            cnt.addComponent(createEntry(data));
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

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(nonConformance, "nonConformance");
                        nonConformance.revalidate();
                    }
                });
            }
        });

        bar.addCommandToOverflowMenu(new Command("New Report ") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Form report = createReportForm();
                report.show();
            }
        });

        bar.addCommandToOverflowMenu(new Command("Remove ") {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }

        });

        nonConformance.show();

        updateScreenFromNetwork(nonConformance, "nonConformance");

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

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(safetyWalk, "safetyWalk");
                        safetyWalk.revalidate();
                    }
                });
            }
        });

        bar.addCommandToOverflowMenu(new Command("New Report ") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Form report = createReportForm();
                report.show();
            }
        });

        bar.addCommandToOverflowMenu(new Command("Remove ") {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }

        });

        safetyWalk.show();

        updateScreenFromNetwork(safetyWalk, "nonConformance");

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

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(plantRegister, "plantRegister");
                        plantRegister.revalidate();
                    }
                });
            }
        });

        bar.addCommandToOverflowMenu(new Command("New Report ") {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Form report = createReportForm();
                report.show();
            }
        });

        bar.addCommandToOverflowMenu(new Command("Remove ") {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }

        });

        plantRegister.show();

        updateScreenFromNetwork(plantRegister, "plantRegister");

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

        bar.addCommandToRightBar(new Command("", theme.getImage("Synch.png").scaled(56, 56)) {

            @Override
            public void actionPerformed(ActionEvent evt) {
                Display.getInstance().callSerially(new Runnable() {

                    public void run() {
                        updateScreenFromNetwork(reports, "reports");
                        reports.revalidate();
                    }
                });
            }
        });

        reports.show();

        updateScreenFromNetwork(reports, "reports");

    }

    public Form createReportForm() {
        Form report = new Form("Report Form");

        report.setLayout(new BorderLayout());
        Toolbar bar = new Toolbar();
        report.setToolBar(bar);
        addMenu(report);

        bar.addCommandToOverflowMenu(new Command("Back ") {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });

        TextArea desc = new TextArea();
        desc.setText("Non Conformance Report");
        desc.setEditable(false);

        report.addComponent(BorderLayout.SOUTH, desc);
        return report;
    }

}
