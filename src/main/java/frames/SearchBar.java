package frames;


import com.alee.laf.text.WebTextField;
import getAscII.GetAscII;
import getIcon.GetIcon;
import hotkeyListener.CheckHotKey;
import main.MainClass;
import search.Search;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static main.MainClass.mainExit;


public class SearchBar {
    private static SearchBar searchBarInstance = new SearchBar();
    private JFrame searchBar = new JFrame();
    private CopyOnWriteArrayList<String> listResult = new CopyOnWriteArrayList<>();
    private JLabel label1 = new JLabel();
    private JLabel label2 = new JLabel();
    private JLabel label3 = new JLabel();
    private JLabel label4 = new JLabel();
    private boolean isOpenLastFolderPressed = false;
    private int labelCount = 0;
    private WebTextField textField;
    private Search search = Search.getInstance();
    private Color labelColor = new Color(255, 152, 104, 255);
    private Color backgroundColor = new Color(108, 108, 108, 255);
    private Color backgroundColorLight = new Color(75, 75, 75, 255);
    private Color fontColorWithCoverage = new Color(0x1C0EFF);
    private long startTime = 0;
    private boolean timer = false;
    private Thread searchWaiter = null;
    private boolean isUsing = false;
    private boolean isRunAsAdminPressed = false;
    private Pattern semicolon = Pattern.compile(";");
    private Pattern resultSplit = Pattern.compile(":");
    private boolean isUserPressed = false;
    private boolean isCommandMode = false;
    private boolean isLockMouseMotion = false;
    private JPanel panel = new JPanel();
    private long mouseWheelTime = 0;
    private boolean isCopyPathPressed = false;


    private SearchBar() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // ��ȡ��Ļ��С
        int width = screenSize.width;
        int height = screenSize.height;
        int searchBarWidth = (int) (width * 0.4);
        int searchBarHeight = (int) (height * 0.5);
        int positionX = width / 2 - searchBarWidth / 2;
        int positionY = height / 2 - searchBarHeight / 2;

        //frame
        searchBar.setBounds(positionX, positionY, searchBarWidth, searchBarHeight);
        searchBar.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        searchBar.setUndecorated(true);
        searchBar.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        searchBar.setBackground(null);
        searchBar.setOpacity(0.8f);
        searchBar.add(panel);
        searchBar.setType(JFrame.Type.UTILITY);// TODO ����������ͼ��


        //labels
        Font font = new Font("Microsoft JhengHei", Font.BOLD, (int) ((height * 0.1) / 96 * 72) / 4);
        Color fontColor = new Color(197, 197, 197, 255);
        label1.setSize(searchBarWidth, (int) (searchBarHeight * 0.2));
        label1.setLocation(0, (int) (searchBarHeight * 0.2));
        label1.setFont(font);
        label1.setForeground(fontColor);
        label1.setOpaque(true);
        label1.setBackground(null);


        label2.setSize(searchBarWidth, (int) (searchBarHeight * 0.2));
        label2.setLocation(0, (int) (searchBarHeight * 0.4));
        label2.setFont(font);
        label2.setForeground(fontColor);
        label2.setOpaque(true);
        label2.setBackground(null);


        label3.setSize(searchBarWidth, (int) (searchBarHeight * 0.2));
        label3.setLocation(0, (int) (searchBarHeight * 0.6));
        label3.setFont(font);
        label3.setForeground(fontColor);
        label3.setOpaque(true);
        label3.setBackground(null);


        label4.setSize(searchBarWidth, (int) (searchBarHeight * 0.2));
        label4.setLocation(0, (int) (searchBarHeight * 0.8));
        label4.setFont(font);
        label4.setForeground(fontColor);
        label4.setOpaque(true);
        label4.setBackground(null);


        URL icon = TaskBar.class.getResource("/icons/taskbar_32x32.png");
        Image image = new ImageIcon(icon).getImage();
        searchBar.setIconImage(image);
        searchBar.setBackground(new Color(0, 0, 0, 0));


        //TextField
        textField = new WebTextField(300);
        textField.setSize(searchBarWidth, (int) (searchBarHeight * 0.2));
        Font textFieldFont = new Font("Microsoft JhengHei", Font.BOLD, (int) (((height * 0.1) / 96 * 72 / 1.2) / 1.5));
        textField.setFont(textFieldFont);
        textField.setForeground(Color.BLACK);
        textField.setHorizontalAlignment(JTextField.LEFT);
        textField.setBorder(null);
        textField.setBackground(Color.WHITE);
        textField.setLocation(0, 0);
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                if (SettingsFrame.isLoseFocusClose) {
                    closedTodo();
                }
            }
        });

        //panel
        panel.setLayout(null);
        panel.setBackground(new Color(0, 0, 0, 0));
        panel.add(textField);
        panel.add(label1);
        panel.add(label2);
        panel.add(label3);
        panel.add(label4);


        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(6);

        fixedThreadPool.execute(() -> {
            //��סMouseMotion��⣬��ֹͬʱ������������
            try {
                while (!mainExit) {
                    if (System.currentTimeMillis() - mouseWheelTime > 500) {
                        isLockMouseMotion = false;
                    }
                    Thread.sleep(5);
                }
            } catch (Exception ignored) {

            }
        });

        //ˢ����Ļ�߳�
        fixedThreadPool.execute(() -> {
            Container contentPanel = searchBar.getContentPane();
            try {
                while (!mainExit) {
                    contentPanel.repaint();
                    panel.repaint();
                    Thread.sleep(100);
                }
            } catch (Exception ignored) {

            }
        });

        fixedThreadPool.execute(() -> {
            try {
                while (!mainExit) {
                    //�ж���ǰѡ��λ��
                    int position;
                    if (label1.getBackground().equals(labelColor)) {
                        position = 0;
                    } else if (label2.getBackground().equals(labelColor)) {
                        position = 1;
                    } else if (label3.getBackground().equals(labelColor)) {
                        position = 2;
                    } else {
                        position = 3;
                    }
                    if (position == 0) {
                        label1.setForeground(fontColorWithCoverage);
                        label2.setForeground(fontColor);
                        label3.setForeground(fontColor);
                        label4.setForeground(fontColor);
                    } else if (position == 1) {
                        label1.setForeground(fontColor);
                        label2.setForeground(fontColorWithCoverage);
                        label3.setForeground(fontColor);
                        label4.setForeground(fontColor);
                    } else if (position == 2) {
                        label1.setForeground(fontColor);
                        label2.setForeground(fontColor);
                        label3.setForeground(fontColorWithCoverage);
                        label4.setForeground(fontColor);
                    } else {
                        label1.setForeground(fontColor);
                        label2.setForeground(fontColor);
                        label3.setForeground(fontColor);
                        label4.setForeground(fontColorWithCoverage);
                    }
                    Thread.sleep(5);
                }
            } catch (Exception ignored) {

            }
        });

        fixedThreadPool.execute(() -> {
            //��ʾ����߳�
            while (!mainExit) {
                if (labelCount < listResult.size()) {//�н��������ʾ
                    try {
                        if (label2.getText().equals("") || label3.getText().equals("") || label4.getText().equals("") || label1.getText().equals("")) {
                            showResults();
                        }
                    } catch (NullPointerException e) {
                        showResults();
                    }
                }
                String text = textField.getText();
                if (text.equals("")) {
                    clearLabel();
                    listResult.clear();
                }
                try {
                    if (!isUserPressed && !label1.getText().equals("")) {
                        if (labelCount == 0) {
                            label1.setBackground(labelColor);
                        } else {
                            label1.setBackground(backgroundColorLight);
                        }
                    }
                    if (!isUserPressed && !label2.getText().equals("")) {
                        if (labelCount == 1) {
                            label2.setBackground(labelColor);
                        } else {
                            label2.setBackground(backgroundColor);
                        }
                    }
                    if (!isUserPressed && !label3.getText().equals("")) {
                        if (labelCount == 2) {
                            label3.setBackground(labelColor);
                        } else {
                            label3.setBackground(backgroundColorLight);
                        }
                    }
                    if (!isUserPressed && !label4.getText().equals("")) {
                        if (labelCount == 3) {
                            label4.setBackground(labelColor);
                        } else {
                            label4.setBackground(backgroundColor);
                        }
                    }
                } catch (NullPointerException ignored) {

                } finally {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {

                    }
                }
            }
        });

        fixedThreadPool.execute(() -> {
            //��⻺���С ����ʱ��������
            try {
                while (!mainExit) {
                    if (!search.isManualUpdate() && !isUsing) {
                        if (search.getRecycleBinSize() > 3000) {
                            System.out.println("�Ѽ�⵽����վ�����Զ�����");
                            search.setUsable(false);
                            search.mergeAndClearRecycleBin();
                            search.setUsable(true);
                        }
                        if (search.getLoadListSize() > 15000) {
                            System.out.println("���ػ���ռ�����Զ�����");
                            search.setUsable(false);
                            search.mergeFileToList();
                            search.setUsable(true);
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException ignored) {

            }
        });

        fixedThreadPool.execute(() -> {
            //����insertUpdate����Ϣ����������
            //ͣ��ʱ��0.5s��ÿһ����������һ��startTime�����̼߳�¼endTime
            while (!mainExit) {
                long endTime = System.currentTimeMillis();
                if ((endTime - startTime > 500) && (timer)) {
                    timer = false; //��ʼ���� ��ʱֹͣ
                    labelCount = 0;
                    clearLabel();
                    if (!textField.getText().equals("")) {
                        label1.setBackground(labelColor);
                    } else {
                        clearLabel();
                    }
                    listResult.clear();
                    String text = textField.getText();
                    if (search.isUsable()) {
                        if (isCommandMode) {
                            if (text.equals(":update")) {
                                clearLabel();
                                MainClass.showMessage("��ʾ", "���ڸ����ļ�����");
                                clearTextFieldText();
                                closedTodo();
                                search.setManualUpdate(true);
                                timer = false;
                                continue;
                            }
                            if (text.equals(":version")) {
                                clearLabel();
                                clearTextFieldText();
                                closedTodo();
                                JOptionPane.showMessageDialog(null, "��ǰ�汾��" + MainClass.version);
                            }
                            if (text.equals(":help")) {
                                clearLabel();
                                clearTextFieldText();
                                closedTodo();
                                JOptionPane.showMessageDialog(null, "������\n" +
                                        "1.Ĭ��Ctrl + Alt + J��������\n" +
                                        "2.Enter�����г���\n" +
                                        "3.Ctrl + Enter���򿪲�ѡ���ļ������ļ���\n" +
                                        "4.Shift + Enter���Թ���ԱȨ�����г���ǰ���Ǹó���ӵ�й���ԱȨ�ޣ�\n" +
                                        "5.��������������  : update  ǿ���ؽ���������\n" +
                                        "6.��������������  : version  �鿴��ǰ�汾\n" +
                                        "7.��������������  : clearbin  ��ջ���վ\n" +
                                        "8.��������������  : help  �鿴����\"\n" +
                                        "9.�������п����Զ��������������������  : �Զ����ʶ  �����Լ�������\n" +
                                        "10.��������ļ���������  : full  ��ȫ��ƥ��\n" +
                                        "11.��������ļ���������  : F  ��ֻƥ���ļ�\n" +
                                        "12.��������ļ���������  : D  ��ֻƥ���ļ���\n" +
                                        "13.��������ļ���������  : Ffull  ��ֻƥ���ļ���ȫ��ƥ��\n" +
                                        "14.��������ļ���������  : Dfull  ��ֻƥ���ļ��в�ȫ��ƥ��");
                            }
                            if (text.equals(":clearbin")) {
                                clearLabel();
                                clearTextFieldText();
                                closedTodo();
                                int r = JOptionPane.showConfirmDialog(null, "��ȷ��Ҫ��ջ���վ��");
                                if (r == 0) {
                                    try {
                                        File[] roots = File.listRoots();
                                        for (File root : roots) {
                                            Runtime.getRuntime().exec("cmd /c rd /s /q " + root.getAbsolutePath() + "$Recycle.Bin");
                                        }
                                        JOptionPane.showMessageDialog(null, "��ջ���վ�ɹ�");
                                    } catch (IOException e) {
                                        JOptionPane.showMessageDialog(null, "��ջ���վʧ��");
                                    }
                                }
                            }
                            for (String i : SettingsFrame.cmdSet) {
                                if (i.startsWith(text)) {
                                    listResult.add("��������" + i);
                                }
                                String[] cmdInfo = semicolon.split(i);
                                if (cmdInfo[0].equals(text)) {
                                    clearLabel();
                                    clearTextFieldText();
                                    closedTodo();
                                    openWithAdmin(cmdInfo[1]);
                                }
                            }
                            showResults();
                        } else {
                            String[] strings;
                            String searchText;
                            int length;
                            String searchCase;
                            strings = resultSplit.split(text);
                            length = strings.length;
                            if (length == 2) {
                                searchCase = strings[1].toUpperCase();
                                searchText = strings[0];
                            } else {
                                searchText = strings[0];
                                searchCase = "";
                            }
                            searchPriorityFolder(searchText, searchCase);
                            searchCache(searchText, searchCase);
                            showResults();

                            HashSet<String> paths = new HashSet<>();
                            String listPath;
                            int ascII = getAscIISum(searchText);

                            if (0 < ascII && ascII <= 100) {
                                for (int i = 0; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (100 < ascII && ascII <= 200) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 100; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (200 < ascII && ascII <= 300) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 200; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (300 < ascII && ascII <= 400) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 300; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (400 < ascII && ascII <= 500) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 400; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (500 < ascII && ascII <= 600) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 500; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (600 < ascII && ascII <= 700) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 600; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (700 < ascII && ascII <= 800) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 700; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (800 < ascII && ascII <= 900) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 800; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (900 < ascII && ascII <= 1000) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 900; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1000 < ascII && ascII <= 1100) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1000; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1100 < ascII && ascII <= 1200) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1100; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1200 < ascII && ascII <= 1300) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1200; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1300 < ascII && ascII <= 1400) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1300; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1400 < ascII && ascII <= 1500) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1400; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1500 < ascII && ascII <= 1600) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1500; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1600 < ascII && ascII <= 1700) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1600; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1700 < ascII && ascII <= 1800) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1700; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1800 < ascII && ascII <= 1900) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1800; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (1900 < ascII && ascII <= 2000) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 1900; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (2000 < ascII && ascII <= 2100) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 2000; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (2100 < ascII && ascII <= 2200) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 2100; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (2200 < ascII && ascII <= 2300) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 2200; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (2300 < ascII && ascII <= 2400) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 2300; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else if (2400 < ascII && ascII <= 2500) {
                                paths.add(SettingsFrame.dataPath + "\\list0-100.txt");
                                for (int i = 2400; i < 2500; i += 100) {
                                    int name = i + 100;
                                    listPath = SettingsFrame.dataPath + "\\list" + i + "-" + name + ".txt";
                                    paths.add(listPath);
                                }
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            } else {
                                paths.add(SettingsFrame.dataPath + "\\list2500-.txt");
                                addResult(paths, searchText, System.currentTimeMillis(), searchCase);
                            }
                        }
                    } else {
                        if (search.isManualUpdate()) {
                            if (searchWaiter == null || !searchWaiter.isAlive()) {
                                searchWaiter = new Thread(() -> {
                                    while (!mainExit) {
                                        if (Thread.currentThread().isInterrupted()) {
                                            break;
                                        }
                                        if (search.isUsable()) {
                                            startTime = System.currentTimeMillis() - 500;
                                            timer = true;
                                            break;
                                        }
                                        try {
                                            Thread.sleep(20);
                                        } catch (InterruptedException ignored) {

                                        }
                                    }
                                });
                                searchWaiter.start();
                            }
                        }
                        clearLabel();
                        if (!search.isUsable()) {
                            label1.setBackground(labelColor);
                            label1.setText("���ڽ�������...");
                        }
                    }
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {

                }
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearLabel();
                listResult.clear();
                labelCount = 0;
                startTime = System.currentTimeMillis();
                timer = true;
                isCommandMode = textField.getText().charAt(0) == ':';
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                clearLabel();
                listResult.clear();
                labelCount = 0;
                String t = textField.getText();

                if (t.equals("")) {
                    clearLabel();
                    listResult.clear();
                    labelCount = 0;
                    startTime = System.currentTimeMillis();
                    timer = false;
                } else {
                    startTime = System.currentTimeMillis();
                    timer = true;
                }
                try {
                    isCommandMode = textField.getText().charAt(0) == ':';
                } catch (StringIndexOutOfBoundsException ignored) {

                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        searchBar.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                int count = e.getClickCount();
                if (count == 2) {
                    closedTodo();
                    if (listResult.size() != 0) {
                        if (!isCommandMode) {
                            if (isOpenLastFolderPressed) {
                                //���ϼ��ļ���
                                File open = new File(listResult.get(labelCount));
                                try {
                                    Runtime.getRuntime().exec("explorer.exe /select, \"" + open.getAbsolutePath() + "\"");
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            } else if (SettingsFrame.isDefaultAdmin || isRunAsAdminPressed) {
                                openWithAdmin(listResult.get(labelCount));
                            } else {
                                String openFile = listResult.get(labelCount);
                                if (openFile.endsWith(".bat") || openFile.endsWith(".cmd")) {
                                    openWithAdmin(openFile);
                                } else {
                                    openWithoutAdmin(openFile);
                                }
                            }
                            saveCache(listResult.get(labelCount) + ';');
                        } else {
                            //ֱ�Ӵ�
                            String command = listResult.get(labelCount);
                            if (Desktop.isDesktopSupported()) {
                                Desktop desktop = Desktop.getDesktop();
                                try {
                                    desktop.open(new File(semicolon.split(command)[1]));
                                } catch (Exception e1) {
                                    JOptionPane.showMessageDialog(null, "ִ��ʧ��");
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        searchBar.addMouseWheelListener(e -> {
            mouseWheelTime = System.currentTimeMillis();
            isLockMouseMotion = true;
            if (e.getPreciseWheelRotation() > 0) {
                //���¹���
                try {
                    if (!label1.getText().equals("") && !label2.getText().equals("") && !label3.getText().equals("") && !label4.getText().equals("")) {
                        isUserPressed = true;
                    }
                } catch (NullPointerException ignored) {

                }
                boolean isNextExist = false;
                if (labelCount == 0) {
                    try {
                        if (!label2.getText().equals("")) {
                            isNextExist = true;
                        }
                    } catch (NullPointerException ignored) {

                    }
                } else if (labelCount == 1) {
                    try {
                        if (!label3.getText().equals("")) {
                            isNextExist = true;
                        }
                    } catch (NullPointerException ignored) {

                    }
                } else if (labelCount == 2) {
                    try {
                        if (!label4.getText().equals("")) {
                            isNextExist = true;
                        }
                    } catch (NullPointerException ignored) {

                    }
                } else {
                    isNextExist = true;
                }
                if (isNextExist) {
                    if (!textField.getText().equals("")) {
                        labelCount++;
                        if (labelCount < 0) {
                            labelCount = 0;
                        }

                        //System.out.println(labelCount);
                        if (labelCount >= listResult.size()) {
                            labelCount = listResult.size() - 1;
                        }
                        //�ж���ǰѡ��λ��
                        int position;
                        try {
                            if (label1.getBackground().equals(labelColor)) {
                                position = 0;
                            } else if (label2.getBackground().equals(labelColor)) {
                                position = 1;
                            } else if (label3.getBackground().equals(labelColor)) {
                                position = 2;
                            } else {
                                position = 3;
                            }
                            if (!isCommandMode) {
                                switch (position) {
                                    case 0:
                                        int size = listResult.size();
                                        if (size == 2) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(labelColor);
                                        } else if (size == 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(labelColor);
                                            label3.setBackground(backgroundColorLight);
                                        } else if (size > 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(labelColor);
                                            label3.setBackground(backgroundColorLight);
                                            label4.setBackground(backgroundColor);
                                        }
                                        break;
                                    case 1:
                                        size = listResult.size();
                                        if (size == 2) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                        } else if (size == 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(labelColor);
                                        } else if (size > 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(labelColor);
                                            label4.setBackground(backgroundColor);
                                        }
                                        break;
                                    case 2:
                                        size = listResult.size();
                                        if (size == 2) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                        } else if (size == 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(backgroundColorLight);
                                        } else if (size > 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(backgroundColorLight);
                                            label4.setBackground(labelColor);
                                        }
                                        break;
                                    case 3:
                                        //�������¶ˣ�ˢ����ʾ
                                        try {
                                            String path = listResult.get(labelCount - 3);
                                            String name = getFileName(listResult.get(labelCount - 3));
                                            ImageIcon icon1;
                                            if (isDirectory(path) || isFile(path)) {
                                                icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                                icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                                label1.setIcon(icon1);
                                                label1.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</font></body></html>");
                                            } else {
                                                label1.setIcon(null);
                                                label1.setText("��Ч�ļ�");
                                                search.addToRecycleBin(path);
                                            }
                                            path = listResult.get(labelCount - 2);
                                            name = getFileName(listResult.get(labelCount - 2));

                                            if (isDirectory(path) || isFile(path)) {
                                                icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                                icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                                label2.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                label2.setIcon(icon1);
                                            } else {
                                                label2.setIcon(null);
                                                label2.setText("��Ч�ļ�");
                                                search.addToRecycleBin(path);
                                            }
                                            path = listResult.get(labelCount - 1);
                                            name = getFileName(listResult.get(labelCount - 1));


                                            if (isDirectory(path) || isFile(path)) {
                                                icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                                icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                                label3.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                label3.setIcon(icon1);
                                            } else {
                                                label3.setIcon(null);
                                                label3.setText("��Ч�ļ�");
                                                search.addToRecycleBin(path);
                                            }
                                            path = listResult.get(labelCount);
                                            name = getFileName(listResult.get(labelCount));


                                            if (isDirectory(path) || isFile(path)) {
                                                icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                                icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                                label4.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                label4.setIcon(icon1);
                                            } else {
                                                label4.setIcon(null);
                                                label4.setText("��Ч�ļ�");
                                                search.addToRecycleBin(path);
                                            }
                                        } catch (ArrayIndexOutOfBoundsException ignored) {

                                        }
                                        break;
                                }
                            } else {
                                switch (position) {
                                    case 0:
                                        int size = listResult.size();
                                        if (size == 2) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(labelColor);
                                        } else if (size == 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(labelColor);
                                            label3.setBackground(backgroundColorLight);
                                        } else if (size > 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(labelColor);
                                            label3.setBackground(backgroundColorLight);
                                            label4.setBackground(backgroundColor);
                                        }
                                        break;
                                    case 1:
                                        size = listResult.size();
                                        if (size == 2) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                        } else if (size == 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(labelColor);
                                        } else if (size > 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(labelColor);
                                            label4.setBackground(backgroundColor);
                                        }
                                        break;
                                    case 2:
                                        size = listResult.size();
                                        if (size == 2) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                        } else if (size == 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(backgroundColorLight);
                                        } else if (size > 3) {
                                            label1.setBackground(backgroundColorLight);
                                            label2.setBackground(backgroundColor);
                                            label3.setBackground(backgroundColorLight);
                                            label4.setBackground(labelColor);
                                        }
                                        break;
                                    case 3:
                                        //���������¶ˣ�ˢ����ʾ
                                        try {
                                            String command = listResult.get(labelCount - 3);
                                            String[] info = semicolon.split(command);
                                            String path = info[1];
                                            String name = info[0];
                                            label1.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                            command = listResult.get(labelCount - 2);
                                            info = semicolon.split(command);
                                            path = info[1];
                                            name = info[0];
                                            label2.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                            command = listResult.get(labelCount - 1);
                                            info = semicolon.split(command);
                                            path = info[1];
                                            name = info[0];
                                            label3.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                            command = listResult.get(labelCount);
                                            info = semicolon.split(command);
                                            path = info[1];
                                            name = info[0];
                                            label4.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                                        } catch (ArrayIndexOutOfBoundsException ignored) {

                                        }
                                        break;
                                }
                            }
                        } catch (NullPointerException ignored) {

                        }
                    }
                }
            } else if (e.getPreciseWheelRotation() < 0) {
                //���Ϲ���
                try {
                    if (!label1.getText().equals("") && !label2.getText().equals("") && !label3.getText().equals("") && !label4.getText().equals("")) {
                        isUserPressed = true;
                    }
                } catch (NullPointerException ignored) {

                }
                if (!textField.getText().equals("")) {
                    labelCount--;
                    if (labelCount < 0) {
                        labelCount = 0;
                    }

                    //System.out.println(labelCount);
                    if (labelCount >= listResult.size()) {
                        labelCount = listResult.size() - 1;
                    }

                    //�ж���ǰѡ��λ��
                    int position;
                    try {
                        if (label1.getBackground().equals(labelColor)) {
                            position = 0;
                        } else if (label2.getBackground().equals(labelColor)) {
                            position = 1;
                        } else if (label3.getBackground().equals(labelColor)) {
                            position = 2;
                        } else {
                            position = 3;
                        }
                        if (!isCommandMode) {
                            switch (position) {
                                case 0:
                                    //���������϶ˣ�ˢ����ʾ
                                    try {
                                        String path = listResult.get(labelCount);
                                        String name = getFileName(listResult.get(labelCount));
                                        ImageIcon icon1;
                                        if (isDirectory(path) || isFile(path)) {
                                            icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                            icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                            label1.setIcon(icon1);
                                            label1.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                        } else {
                                            label1.setIcon(null);
                                            label1.setText("��Ч�ļ�");
                                            search.addToRecycleBin(path);
                                        }
                                        path = listResult.get(labelCount + 1);
                                        name = getFileName(listResult.get(labelCount + 1));

                                        if (isDirectory(path) || isFile(path)) {
                                            icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                            icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                            label2.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                            label2.setIcon(icon1);
                                        } else {
                                            label2.setIcon(null);
                                            label2.setText("��Ч�ļ�");
                                            search.addToRecycleBin(path);
                                        }
                                        path = listResult.get(labelCount + 2);
                                        name = getFileName(listResult.get(labelCount + 2));


                                        if (isDirectory(path) || isFile(path)) {
                                            icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                            icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                            label3.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                            label3.setIcon(icon1);
                                        } else {
                                            label3.setIcon(null);
                                            label3.setText("��Ч�ļ�");
                                            search.addToRecycleBin(path);
                                        }
                                        path = listResult.get(labelCount + 3);
                                        name = getFileName(listResult.get(labelCount + 3));


                                        if (isDirectory(path) || isFile(path)) {
                                            icon1 = (ImageIcon) GetIcon.getBigIcon(path);
                                            icon1 = changeIcon(icon1, label1.getHeight() / 3, label1.getHeight() / 3);
                                            label4.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                            label4.setIcon(icon1);
                                        } else {
                                            label4.setIcon(null);
                                            label4.setText("��Ч�ļ�");
                                            search.addToRecycleBin(path);
                                        }
                                    } catch (ArrayIndexOutOfBoundsException ignored) {

                                    }
                                    break;
                                case 1:
                                    try {
                                        if (!label1.getText().equals("")) {
                                            label1.setBackground(labelColor);
                                        }
                                        if (!label2.getText().equals("")) {
                                            label2.setBackground(backgroundColor);
                                        }
                                        if (!label3.getText().equals("")) {
                                            label3.setBackground(backgroundColorLight);
                                        }
                                        if (!label4.getText().equals("")) {
                                            label4.setBackground(backgroundColor);
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                    break;
                                case 2:
                                    try {
                                        if (!label1.getText().equals("")) {
                                            label1.setBackground(backgroundColorLight);
                                        }
                                        if (!label2.getText().equals("")) {
                                            label2.setBackground(labelColor);
                                        }
                                        if (!label3.getText().equals("")) {
                                            label3.setBackground(backgroundColorLight);
                                        }
                                        if (!label4.getText().equals("")) {
                                            label4.setBackground(backgroundColor);
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                    break;
                                case 3:
                                    try {
                                        if (!label1.getText().equals("")) {
                                            label1.setBackground(backgroundColorLight);
                                        }
                                        if (!label2.getText().equals("")) {
                                            label2.setBackground(backgroundColor);
                                        }
                                        if (!label3.getText().equals("")) {
                                            label3.setBackground(labelColor);
                                        }
                                        if (!label4.getText().equals("")) {
                                            label4.setBackground(backgroundColor);
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                    break;
                            }
                        } else {
                            switch (position) {
                                case 0:
                                    //���������϶ˣ�ˢ����ʾ
                                    try {
                                        String command = listResult.get(labelCount);
                                        String[] info = semicolon.split(command);
                                        String path = info[1];
                                        String name = info[0];
                                        label1.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                        command = listResult.get(labelCount + 1);
                                        info = semicolon.split(command);
                                        path = info[1];
                                        name = info[0];
                                        label2.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                        command = listResult.get(labelCount + 2);
                                        info = semicolon.split(command);
                                        path = info[1];
                                        name = info[0];
                                        label3.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                        command = listResult.get(labelCount + 3);
                                        info = semicolon.split(command);
                                        path = info[1];
                                        name = info[0];
                                        label4.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                                    } catch (ArrayIndexOutOfBoundsException ignored) {

                                    }
                                    break;
                                case 1:
                                    try {
                                        if (!label1.getText().equals("")) {
                                            label1.setBackground(labelColor);
                                        }
                                        if (!label2.getText().equals("")) {
                                            label2.setBackground(backgroundColor);
                                        }
                                        if (!label3.getText().equals("")) {
                                            label3.setBackground(backgroundColorLight);
                                        }
                                        if (!label4.getText().equals("")) {
                                            label4.setBackground(backgroundColor);
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                    break;
                                case 2:
                                    try {
                                        if (!label1.getText().equals("")) {
                                            label1.setBackground(backgroundColorLight);
                                        }
                                        if (!label2.getText().equals("")) {
                                            label2.setBackground(labelColor);
                                        }
                                        if (!label3.getText().equals("")) {
                                            label3.setBackground(backgroundColorLight);
                                        }
                                        if (!label4.getText().equals("")) {
                                            label4.setBackground(backgroundColor);
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                    break;
                                case 3:
                                    try {
                                        if (!label1.getText().equals("")) {
                                            label1.setBackground(backgroundColorLight);
                                        }
                                        if (!label2.getText().equals("")) {
                                            label2.setBackground(backgroundColor);
                                        }
                                        if (!label3.getText().equals("")) {
                                            label3.setBackground(labelColor);
                                        }
                                        if (!label4.getText().equals("")) {
                                            label4.setBackground(backgroundColor);
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                    break;
                                case 4:
                                    try {
                                        if (!label1.getText().equals("")) {
                                            label1.setBackground(backgroundColorLight);
                                        }
                                        if (!label2.getText().equals("")) {
                                            label2.setBackground(backgroundColor);
                                        }
                                        if (!label3.getText().equals("")) {
                                            label3.setBackground(backgroundColorLight);
                                        }
                                        if (!label4.getText().equals("")) {
                                            label4.setBackground(labelColor);
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                    break;
                            }
                        }
                    } catch (NullPointerException ignored) {

                    }

                    if (labelCount < 0) {
                        labelCount = 0;
                    }
                }

            }
        });

        //�ж����λ��
        int labelPosition = label1.getY();
        int labelPosition2 = labelPosition * 2;
        int labelPosition3 = labelPosition * 3;
        int labelPosition4 = labelPosition * 4;
        int end = labelPosition * 5;
        searchBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                //�ж���ǰλ��
                if (!isLockMouseMotion) {
                    int position;
                    if (label1.getBackground().equals(labelColor)) {
                        position = 0;
                    } else if (label2.getBackground().equals(labelColor)) {
                        position = 1;
                    } else if (label3.getBackground().equals(labelColor)) {
                        position = 2;
                    } else {
                        position = 3;
                    }
                    int mousePosition = 0;
                    if (labelPosition <= e.getY() && e.getY() < labelPosition2) {
                    } else if (labelPosition2 <= e.getY() && e.getY() < labelPosition3) {
                        mousePosition = 1;
                    } else if (labelPosition3 <= e.getY() && e.getY() < labelPosition4) {
                        mousePosition = 2;
                    } else if (labelPosition4 <= e.getY() && e.getY() < end) {
                        mousePosition = 3;
                    }
                    if (mousePosition < listResult.size()) {
                        if (position < mousePosition) {
                            labelCount = labelCount + (mousePosition - position);
                        } else {
                            labelCount = labelCount - (position - mousePosition);
                        }
                        switch (mousePosition) {
                            case 0:
                                try {
                                    if (!label1.getText().equals("")) {
                                        label1.setBackground(labelColor);
                                    }
                                    if (!label2.getText().equals("")) {
                                        label2.setBackground(backgroundColor);
                                    }
                                    if (!label3.getText().equals("")) {
                                        label3.setBackground(backgroundColorLight);
                                    }
                                    if (!label4.getText().equals("")) {
                                        label4.setBackground(backgroundColor);
                                    }
                                } catch (NullPointerException ignored) {

                                }
                                break;
                            case 1:
                                try {
                                    if (!label1.getText().equals("")) {
                                        label1.setBackground(backgroundColorLight);
                                    }
                                    if (!label2.getText().equals("")) {
                                        label2.setBackground(labelColor);
                                    }
                                    if (!label3.getText().equals("")) {
                                        label3.setBackground(backgroundColorLight);
                                    }
                                    if (!label4.getText().equals("")) {
                                        label4.setBackground(backgroundColor);
                                    }
                                } catch (NullPointerException ignored) {

                                }
                                break;
                            case 2:
                                try {
                                    if (!label1.getText().equals("")) {
                                        label1.setBackground(backgroundColorLight);
                                    }
                                    if (!label2.getText().equals("")) {
                                        label2.setBackground(backgroundColor);
                                    }
                                    if (!label3.getText().equals("")) {
                                        label3.setBackground(labelColor);
                                    }
                                    if (!label4.getText().equals("")) {
                                        label4.setBackground(backgroundColor);
                                    }
                                } catch (NullPointerException ignored) {

                                }
                                break;
                            case 3:
                                try {
                                    if (!label1.getText().equals("")) {
                                        label1.setBackground(backgroundColorLight);
                                    }
                                    if (!label2.getText().equals("")) {
                                        label2.setBackground(backgroundColor);
                                    }
                                    if (!label3.getText().equals("")) {
                                        label3.setBackground(backgroundColorLight);
                                    }
                                    if (!label4.getText().equals("")) {
                                        label4.setBackground(labelColor);
                                    }
                                } catch (NullPointerException ignored) {

                                }
                                break;
                        }
                    }
                }
            }
        });

        textField.addKeyListener(new KeyListener() {
            int timeLimit = 50;
            long pressTime;
            boolean isFirstPress = true;

            @Override
            public void keyPressed(KeyEvent arg0) {
                if (!listResult.isEmpty()) {
                    int key = arg0.getKeyCode();
                    if (38 == key) {
                        //�ϼ������
                        if (isFirstPress || System.currentTimeMillis() - pressTime > timeLimit) {
                            pressTime = System.currentTimeMillis();
                            isFirstPress = false;
                            try {
                                if (!label1.getText().equals("") && !label2.getText().equals("") && !label3.getText().equals("") && !label4.getText().equals("")) {
                                    isUserPressed = true;
                                }
                            } catch (NullPointerException ignored) {

                            }
                            if (!textField.getText().equals("")) {
                                labelCount--;
                                if (labelCount < 0) {
                                    labelCount = 0;
                                }

                                //System.out.println(labelCount);
                                if (labelCount >= listResult.size()) {
                                    labelCount = listResult.size() - 1;
                                }

                                //�ж���ǰѡ��λ��
                                int position;
                                try {
                                    if (label1.getBackground().equals(labelColor)) {
                                        position = 0;
                                    } else if (label2.getBackground().equals(labelColor)) {
                                        position = 1;
                                    } else if (label3.getBackground().equals(labelColor)) {
                                        position = 2;
                                    } else {
                                        position = 3;
                                    }
                                    if (!isCommandMode) {
                                        switch (position) {
                                            case 0:
                                                //���������϶ˣ�ˢ����ʾ
                                                try {
                                                    String path = listResult.get(labelCount);
                                                    String name = getFileName(listResult.get(labelCount));
                                                    ImageIcon icon;
                                                    if (isDirectory(path) || isFile(path)) {
                                                        icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                        icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                        label1.setIcon(icon);
                                                        label1.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                    } else {
                                                        label1.setIcon(null);
                                                        label1.setText("��Ч�ļ�");
                                                        search.addToRecycleBin(path);
                                                    }
                                                    path = listResult.get(labelCount + 1);
                                                    name = getFileName(listResult.get(labelCount + 1));

                                                    if (isDirectory(path) || isFile(path)) {
                                                        icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                        icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                        label2.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                        label2.setIcon(icon);
                                                    } else {
                                                        label2.setIcon(null);
                                                        label2.setText("��Ч�ļ�");
                                                        search.addToRecycleBin(path);
                                                    }
                                                    path = listResult.get(labelCount + 2);
                                                    name = getFileName(listResult.get(labelCount + 2));


                                                    if (isDirectory(path) || isFile(path)) {
                                                        icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                        icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                        label3.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                        label3.setIcon(icon);
                                                    } else {
                                                        label3.setIcon(null);
                                                        label3.setText("��Ч�ļ�");
                                                        search.addToRecycleBin(path);
                                                    }
                                                    path = listResult.get(labelCount + 3);
                                                    name = getFileName(listResult.get(labelCount + 3));


                                                    if (isDirectory(path) || isFile(path)) {
                                                        icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                        icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                        label4.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                        label4.setIcon(icon);
                                                    } else {
                                                        label4.setIcon(null);
                                                        label4.setText("��Ч�ļ�");
                                                        search.addToRecycleBin(path);
                                                    }
                                                } catch (ArrayIndexOutOfBoundsException ignored) {

                                                }
                                                break;
                                            case 1:
                                                try {
                                                    if (!label1.getText().equals("")) {
                                                        label1.setBackground(labelColor);
                                                    }
                                                    if (!label2.getText().equals("")) {
                                                        label2.setBackground(backgroundColor);
                                                    }
                                                    if (!label3.getText().equals("")) {
                                                        label3.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label4.getText().equals("")) {
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                } catch (NullPointerException ignored) {

                                                }
                                                break;
                                            case 2:
                                                try {
                                                    if (!label1.getText().equals("")) {
                                                        label1.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label2.getText().equals("")) {
                                                        label2.setBackground(labelColor);
                                                    }
                                                    if (!label3.getText().equals("")) {
                                                        label3.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label4.getText().equals("")) {
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                } catch (NullPointerException ignored) {

                                                }
                                                break;
                                            case 3:
                                                try {
                                                    if (!label1.getText().equals("")) {
                                                        label1.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label2.getText().equals("")) {
                                                        label2.setBackground(backgroundColor);
                                                    }
                                                    if (!label3.getText().equals("")) {
                                                        label3.setBackground(labelColor);
                                                    }
                                                    if (!label4.getText().equals("")) {
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                } catch (NullPointerException ignored) {

                                                }
                                                break;
                                        }
                                    } else {
                                        switch (position) {
                                            case 0:
                                                //���������϶ˣ�ˢ����ʾ
                                                try {
                                                    String command = listResult.get(labelCount);
                                                    String[] info = semicolon.split(command);
                                                    String path = info[1];
                                                    String name = info[0];
                                                    label1.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                                    command = listResult.get(labelCount + 1);
                                                    info = semicolon.split(command);
                                                    path = info[1];
                                                    name = info[0];
                                                    label2.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                                    command = listResult.get(labelCount + 2);
                                                    info = semicolon.split(command);
                                                    path = info[1];
                                                    name = info[0];
                                                    label3.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                                    command = listResult.get(labelCount + 3);
                                                    info = semicolon.split(command);
                                                    path = info[1];
                                                    name = info[0];
                                                    label4.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                                                } catch (ArrayIndexOutOfBoundsException ignored) {

                                                }
                                                break;
                                            case 1:
                                                try {
                                                    if (!label1.getText().equals("")) {
                                                        label1.setBackground(labelColor);
                                                    }
                                                    if (!label2.getText().equals("")) {
                                                        label2.setBackground(backgroundColor);
                                                    }
                                                    if (!label3.getText().equals("")) {
                                                        label3.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label4.getText().equals("")) {
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                } catch (NullPointerException ignored) {

                                                }
                                                break;
                                            case 2:
                                                try {
                                                    if (!label1.getText().equals("")) {
                                                        label1.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label2.getText().equals("")) {
                                                        label2.setBackground(labelColor);
                                                    }
                                                    if (!label3.getText().equals("")) {
                                                        label3.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label4.getText().equals("")) {
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                } catch (NullPointerException ignored) {

                                                }
                                                break;
                                            case 3:
                                                try {
                                                    if (!label1.getText().equals("")) {
                                                        label1.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label2.getText().equals("")) {
                                                        label2.setBackground(backgroundColor);
                                                    }
                                                    if (!label3.getText().equals("")) {
                                                        label3.setBackground(labelColor);
                                                    }
                                                    if (!label4.getText().equals("")) {
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                } catch (NullPointerException ignored) {

                                                }
                                                break;
                                            case 4:
                                                try {
                                                    if (!label1.getText().equals("")) {
                                                        label1.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label2.getText().equals("")) {
                                                        label2.setBackground(backgroundColor);
                                                    }
                                                    if (!label3.getText().equals("")) {
                                                        label3.setBackground(backgroundColorLight);
                                                    }
                                                    if (!label4.getText().equals("")) {
                                                        label4.setBackground(labelColor);
                                                    }
                                                } catch (NullPointerException ignored) {

                                                }
                                                break;
                                        }
                                    }
                                } catch (NullPointerException ignored) {

                                }

                            }
                            if (labelCount < 0) {
                                labelCount = 0;
                            }
                        }
                    } else if (40 == key) {
                        //�¼������
                        if (isFirstPress || System.currentTimeMillis() - pressTime > timeLimit) {
                            pressTime = System.currentTimeMillis();
                            isFirstPress = false;
                            try {
                                if (!label1.getText().equals("") && !label2.getText().equals("") && !label3.getText().equals("") && !label4.getText().equals("")) {
                                    isUserPressed = true;
                                }
                            } catch (NullPointerException ignored) {

                            }
                            boolean isNextExist = false;
                            if (labelCount == 0) {
                                try {
                                    if (!label2.getText().equals("")) {
                                        isNextExist = true;
                                    }
                                } catch (NullPointerException ignored) {

                                }
                            } else if (labelCount == 1) {
                                try {
                                    if (!label3.getText().equals("")) {
                                        isNextExist = true;
                                    }
                                } catch (NullPointerException ignored) {

                                }
                            } else if (labelCount == 2) {
                                try {
                                    if (!label4.getText().equals("")) {
                                        isNextExist = true;
                                    }
                                } catch (NullPointerException ignored) {

                                }
                            } else {
                                isNextExist = true;
                            }
                            if (isNextExist) {
                                if (!textField.getText().equals("")) {
                                    labelCount++;
                                    if (labelCount < 0) {
                                        labelCount = 0;
                                    }

                                    //System.out.println(labelCount);
                                    if (labelCount >= listResult.size()) {
                                        labelCount = listResult.size() - 1;
                                    }
                                    //�ж���ǰѡ��λ��
                                    int position;
                                    try {
                                        if (label1.getBackground().equals(labelColor)) {
                                            position = 0;
                                        } else if (label2.getBackground().equals(labelColor)) {
                                            position = 1;
                                        } else if (label3.getBackground().equals(labelColor)) {
                                            position = 2;
                                        } else {
                                            position = 3;
                                        }
                                        if (!isCommandMode) {
                                            switch (position) {
                                                case 0:
                                                    int size = listResult.size();
                                                    if (size == 2) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(labelColor);
                                                    } else if (size == 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(labelColor);
                                                        label3.setBackground(backgroundColorLight);
                                                    } else if (size > 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(labelColor);
                                                        label3.setBackground(backgroundColorLight);
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                    break;
                                                case 1:
                                                    size = listResult.size();
                                                    if (size == 2) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                    } else if (size == 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(labelColor);
                                                    } else if (size > 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(labelColor);
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                    break;
                                                case 2:
                                                    size = listResult.size();
                                                    if (size == 2) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                    } else if (size == 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(backgroundColorLight);
                                                    } else if (size > 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(backgroundColorLight);
                                                        label4.setBackground(labelColor);
                                                    }
                                                    break;
                                                case 3:
                                                    //�������¶ˣ�ˢ����ʾ
                                                    try {
                                                        String path = listResult.get(labelCount - 3);
                                                        String name = getFileName(listResult.get(labelCount - 3));
                                                        ImageIcon icon;
                                                        if (isDirectory(path) || isFile(path)) {
                                                            icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                            icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                            label1.setIcon(icon);
                                                            label1.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                        } else {
                                                            label1.setIcon(null);
                                                            label1.setText("��Ч�ļ�");
                                                            search.addToRecycleBin(path);
                                                        }
                                                        path = listResult.get(labelCount - 2);
                                                        name = getFileName(listResult.get(labelCount - 2));

                                                        if (isDirectory(path) || isFile(path)) {
                                                            icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                            icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                            label2.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                            label2.setIcon(icon);
                                                        } else {
                                                            label2.setIcon(null);
                                                            label2.setText("��Ч�ļ�");
                                                            search.addToRecycleBin(path);
                                                        }
                                                        path = listResult.get(labelCount - 1);
                                                        name = getFileName(listResult.get(labelCount - 1));


                                                        if (isDirectory(path) || isFile(path)) {
                                                            icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                            icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                            label3.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                            label3.setIcon(icon);
                                                        } else {
                                                            label3.setIcon(null);
                                                            label3.setText("��Ч�ļ�");
                                                            search.addToRecycleBin(path);
                                                        }
                                                        path = listResult.get(labelCount);
                                                        name = getFileName(listResult.get(labelCount));


                                                        if (isDirectory(path) || isFile(path)) {
                                                            icon = (ImageIcon) GetIcon.getBigIcon(path);
                                                            icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                                                            label4.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                                                            label4.setIcon(icon);
                                                        } else {
                                                            label4.setIcon(null);
                                                            label4.setText("��Ч�ļ�");
                                                            search.addToRecycleBin(path);
                                                        }
                                                    } catch (ArrayIndexOutOfBoundsException ignored) {

                                                    }
                                                    break;
                                            }
                                        } else {
                                            switch (position) {
                                                case 0:
                                                    int size = listResult.size();
                                                    if (size == 2) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(labelColor);
                                                    } else if (size == 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(labelColor);
                                                        label3.setBackground(backgroundColorLight);
                                                    } else if (size > 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(labelColor);
                                                        label3.setBackground(backgroundColorLight);
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                    break;
                                                case 1:
                                                    size = listResult.size();
                                                    if (size == 2) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                    } else if (size == 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(labelColor);
                                                    } else if (size > 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(labelColor);
                                                        label4.setBackground(backgroundColor);
                                                    }
                                                    break;
                                                case 2:
                                                    size = listResult.size();
                                                    if (size == 2) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                    } else if (size == 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(backgroundColorLight);
                                                    } else if (size > 3) {
                                                        label1.setBackground(backgroundColorLight);
                                                        label2.setBackground(backgroundColor);
                                                        label3.setBackground(backgroundColorLight);
                                                        label4.setBackground(labelColor);
                                                    }
                                                    break;
                                                case 3:
                                                    //���������¶ˣ�ˢ����ʾ
                                                    try {
                                                        String command = listResult.get(labelCount - 3);
                                                        String[] info = semicolon.split(command);
                                                        String path = info[1];
                                                        String name = info[0];
                                                        label1.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                                        command = listResult.get(labelCount - 2);
                                                        info = semicolon.split(command);
                                                        path = info[1];
                                                        name = info[0];
                                                        label2.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                                        command = listResult.get(labelCount - 1);
                                                        info = semicolon.split(command);
                                                        path = info[1];
                                                        name = info[0];
                                                        label3.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");

                                                        command = listResult.get(labelCount);
                                                        info = semicolon.split(command);
                                                        path = info[1];
                                                        name = info[0];
                                                        label4.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                                                    } catch (ArrayIndexOutOfBoundsException ignored) {

                                                    }
                                                    break;
                                            }
                                        }
                                    } catch (NullPointerException ignored) {

                                    }
                                }
                            }
                        }
                    } else if (10 == key) {
                        //enter�����
                        closedTodo();
                        if (!isCommandMode) {
                            if (isOpenLastFolderPressed) {
                                //���ϼ��ļ���
                                File open = new File(listResult.get(labelCount));
                                try {
                                    Runtime.getRuntime().exec("explorer.exe /select, \"" + open.getAbsolutePath() + "\"");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (SettingsFrame.isDefaultAdmin || isRunAsAdminPressed) {
                                openWithAdmin(listResult.get(labelCount));
                            } else if (isCopyPathPressed) {
                                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                Transferable trans = new StringSelection(listResult.get(labelCount));
                                clipboard.setContents(trans, null);
                            } else {
                                String openFile = listResult.get(labelCount);
                                if (openFile.endsWith(".bat") || openFile.endsWith(".cmd")) {
                                    openWithAdmin(openFile);
                                } else {
                                    openWithoutAdmin(openFile);
                                }
                            }
                            saveCache(listResult.get(labelCount) + ';');
                        } else {
                            //ֱ�Ӵ�
                            String command = listResult.get(labelCount);
                            if (Desktop.isDesktopSupported()) {
                                Desktop desktop = Desktop.getDesktop();
                                try {
                                    desktop.open(new File(semicolon.split(command)[1]));
                                } catch (Exception e) {
                                    JOptionPane.showMessageDialog(null, "ִ��ʧ��");
                                }
                            }
                        }
                    } else if (SettingsFrame.openLastFolderKeyCode == key) {
                        //���ϼ��ļ����ȼ������
                        isOpenLastFolderPressed = true;
                    } else if (SettingsFrame.runAsAdminKeyCode == key) {
                        //�Թ���Ա��ʽ�����ȼ������
                        isRunAsAdminPressed = true;
                    } else if (SettingsFrame.copyPathKeyCode == key) {
                        isCopyPathPressed = true;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                int key = arg0.getKeyCode();
                if (SettingsFrame.openLastFolderKeyCode == key) {
                    //��λ����״̬
                    isOpenLastFolderPressed = false;
                } else if (SettingsFrame.runAsAdminKeyCode == key) {
                    isRunAsAdminPressed = false;
                } else if (SettingsFrame.copyPathKeyCode == key) {
                    isCopyPathPressed = false;
                }
            }

            @Override
            public void keyTyped(KeyEvent arg0) {

            }
        });
    }

    public static SearchBar getInstance() {
        return searchBarInstance;
    }

    public boolean isUsing() {
        //�����Ƿ�������ʾ
        return this.isUsing;
    }

    private boolean isExist(String path) {
        File f = new File(path);
        return f.exists();
    }

    private void addResult(HashSet<String> paths, String searchText, long time, String searchCase) {
        //Ϊlabel���ӽ��
        String each;
        for (String path : paths) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
                while ((each = br.readLine()) != null) {
                    if (startTime > time) { //�û�������������Ϣ
                        return;
                    }
                    if (search.isUsable()) {
                        if (isSubString(getFileName(each), searchText)) {
                            switch (searchCase) {
                                case "F":
                                    if (isFile(each)) {
                                        if (!listResult.contains(each)) {
                                            if (isExist(each)) {
                                                listResult.add(each);
                                                if (listResult.size() > 100) {
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "D":
                                    if (isDirectory(each)) {
                                        if (!listResult.contains(each)) {
                                            if (isExist(each)) {
                                                listResult.add(each);
                                                if (listResult.size() > 100) {
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "FULL":
                                    if (getFileName(each).toLowerCase().equals(searchText.toLowerCase())) {
                                        if (!listResult.contains(each)) {
                                            if (isExist(each)) {
                                                listResult.add(each);
                                                if (listResult.size() > 100) {
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "DFULL":
                                    if (getFileName(each).toLowerCase().equals(searchText.toLowerCase())) {
                                        if (isDirectory(each)) {
                                            if (!listResult.contains(each)) {
                                                if (isExist(each)) {
                                                    listResult.add(each);
                                                    if (listResult.size() > 100) {
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "FFULL":
                                    if (getFileName(each).toLowerCase().equals(searchText.toLowerCase())) {
                                        if (isFile(each)) {
                                            if (!listResult.contains(each)) {
                                                if (isExist(each)) {
                                                    listResult.add(each);
                                                    if (listResult.size() > 100) {
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    if (!listResult.contains(each)) {
                                        if (isExist(each)) {
                                            listResult.add(each);
                                            if (listResult.size() > 100) {
                                                return;
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            } catch (IOException ignored) {

            }
        }
        if (!textField.getText().equals("")) {
            delRepeated();
            if (listResult.size() == 0) {
                label1.setText("�޽��");
            }
        }
    }

    public void showSearchbar() {
        textField.setCaretPosition(0);
        //���Ӹ����ļ�
        textField.requestFocusInWindow();
        isUsing = true;
        searchBar.setVisible(true);
    }

    private void showResults() {
        if (!isCommandMode) {
            try {
                String path = listResult.get(0);
                String name = getFileName(listResult.get(0));
                ImageIcon icon;
                if (isDirectory(path) || isFile(path)) {
                    icon = (ImageIcon) GetIcon.getBigIcon(path);
                    icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                    label1.setIcon(icon);
                    label1.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                    if (labelCount == 0) {
                        label1.setBackground(labelColor);
                    } else {
                        label1.setBackground(backgroundColorLight);
                    }
                } else {
                    label1.setIcon(null);
                    label1.setText("��Ч�ļ�");
                    if (labelCount == 0) {
                        label1.setBackground(labelColor);
                    } else {
                        label1.setBackground(backgroundColorLight);
                    }
                    search.addToRecycleBin(path);
                }
                path = listResult.get(1);
                name = getFileName(listResult.get(1));


                if (isDirectory(path) || isFile(path)) {
                    icon = (ImageIcon) GetIcon.getBigIcon(path);
                    icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                    label2.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                    label2.setIcon(icon);
                    if (labelCount == 1) {
                        label2.setBackground(labelColor);
                    } else {
                        label2.setBackground(backgroundColor);
                    }
                } else {
                    label2.setIcon(null);
                    label2.setText("��Ч�ļ�");
                    if (labelCount == 1) {
                        label2.setBackground(labelColor);
                    } else {
                        label2.setBackground(backgroundColor);
                    }
                    search.addToRecycleBin(path);
                }
                path = listResult.get(2);
                name = getFileName(listResult.get(2));


                if (isDirectory(path) || isFile(path)) {
                    icon = (ImageIcon) GetIcon.getBigIcon(path);
                    icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                    label3.setIcon(icon);
                    label3.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                    if (labelCount == 2) {
                        label3.setBackground(labelColor);
                    } else {
                        label3.setBackground(backgroundColorLight);
                    }
                } else {
                    label3.setIcon(null);
                    label3.setText("��Ч�ļ�");
                    if (labelCount == 2) {
                        label3.setBackground(labelColor);
                    } else {
                        label3.setBackground(backgroundColorLight);
                    }
                    search.addToRecycleBin(path);
                }
                path = listResult.get(3);
                name = getFileName(listResult.get(3));


                if (isDirectory(path) || isFile(path)) {
                    icon = (ImageIcon) GetIcon.getBigIcon(path);
                    icon = changeIcon(icon, label1.getHeight() / 3, label1.getHeight() / 3);
                    label4.setText("<html><body>" + name + "<br><font size=\"-1\">" + ">>>" + getParentPath(path) + "</body></html>");
                    label4.setIcon(icon);
                    if (labelCount >= 3) {
                        label4.setBackground(labelColor);
                    } else {
                        label4.setBackground(backgroundColor);
                    }
                } else {
                    label4.setIcon(null);
                    label4.setText("��Ч�ļ�");
                    if (labelCount >= 3) {
                        label4.setBackground(labelColor);
                    } else {
                        label4.setBackground(backgroundColor);
                    }
                    search.addToRecycleBin(path);
                }
            } catch (java.lang.IndexOutOfBoundsException ignored) {

            }
        } else {
            try {
                String command = listResult.get(0);
                String[] info = semicolon.split(command);
                String path = info[1];
                String name = info[0];
                label1.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                if (labelCount == 0) {
                    label1.setBackground(labelColor);
                } else {
                    label1.setBackground(backgroundColorLight);
                }

                command = listResult.get(1);
                info = semicolon.split(command);
                path = info[1];
                name = info[0];
                label2.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                if (labelCount == 1) {
                    label2.setBackground(labelColor);
                } else {
                    label2.setBackground(backgroundColor);
                }

                command = listResult.get(2);
                info = semicolon.split(command);
                path = info[1];
                name = info[0];
                label3.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                if (labelCount == 2) {
                    label3.setBackground(labelColor);
                } else {
                    label3.setBackground(backgroundColorLight);
                }

                command = listResult.get(3);
                info = semicolon.split(command);
                path = info[1];
                name = info[0];
                label4.setText("<html><body>" + "&nbsp;&nbsp;" + "&nbsp;&nbsp;" + name + "<br>&nbsp;&nbsp;" + "<font size=\"-1\">" + "&nbsp;&nbsp;" + ">>>" + path + "</font></body></html>");
                if (labelCount >= 3) {
                    label4.setBackground(labelColor);
                } else {
                    label4.setBackground(backgroundColor);
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {

            }
        }
    }


    private void clearLabel() {
        label1.setIcon(null);
        label2.setIcon(null);
        label3.setIcon(null);
        label4.setIcon(null);
        label1.setText(null);
        label2.setText(null);
        label3.setText(null);
        label4.setText(null);
        label1.setBackground(null);
        label2.setBackground(null);
        label3.setBackground(null);
        label4.setBackground(null);
    }

    private void openWithAdmin(String path) {
        File name = new File(path);
        if (name.exists()) {
            try {
                String command = name.getAbsolutePath();
                String start = "cmd /c start " + command.substring(0, 2);
                String end = "\"" + command.substring(2) + "\"";
                Runtime.getRuntime().exec(start + end, null, name.getParentFile());
            } catch (IOException e) {
                //���ϼ��ļ���
                try {
                    Runtime.getRuntime().exec("explorer.exe /select, \"" + name.getAbsolutePath() + "\"");
                } catch (IOException ignored) {

                }
            }
        }
    }

    private void openWithoutAdmin(String path) {
        File name = new File(path);
        if (name.exists()) {
            try {
                if (path.endsWith(".exe")) {
                    Runtime.getRuntime().exec("cmd /c runas /trustlevel:0x20000 \"" + name.getAbsolutePath() + "\"", null, name.getParentFile());
                } else {
                    File fileToOpen = new File("user/fileToOpen.txt");
                    File fileOpener = new File("user/fileOpener.exe");
                    try (BufferedWriter buffw = new BufferedWriter(new FileWriter(fileToOpen))) {
                        buffw.write(name.getAbsolutePath() + "\n");
                        buffw.write(name.getParent());
                    }
                    Runtime.getRuntime().exec("cmd /c runas /trustlevel:0x20000 \"" + fileOpener.getAbsolutePath() + "\"", null, fileOpener.getParentFile());
                }
            } catch (IOException e) {
                //���ϼ��ļ���
                try {
                    Runtime.getRuntime().exec("explorer.exe /select, \"" + name.getAbsolutePath() + "\"");
                } catch (IOException ignored) {

                }
            }
        }
    }

    public String getFileName(String path) {
        File file = new File(path);
        return file.getName();
    }

    private ImageIcon changeIcon(ImageIcon icon, int width, int height) {
        try {
            Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);
            return new ImageIcon(image);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public int getAscIISum(String path) {
        path = path.toUpperCase();
        return GetAscII.INSTANCE.getAscII(path);
    }

    private void saveCache(String content) {
        int cacheNum = 0;
        File cache = new File("user/cache.dat");
        StringBuilder oldCaches = new StringBuilder();
        boolean isRepeated;
        if (cache.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(cache))) {
                String eachLine;
                while ((eachLine = reader.readLine()) != null) {
                    oldCaches.append(eachLine);
                    cacheNum++;
                }
            } catch (IOException ignored) {

            }
        }
        if (cacheNum < SettingsFrame.cacheNumLimit) {
            isRepeated = isSubString(oldCaches.toString() + ";", (content));
            if (!isRepeated) {
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("user/cache.dat"), true)))) {
                    out.write(content + "\r\n");
                } catch (Exception ignored) {

                }
            }
        }
    }

    private void delCacheRepeated() {
        File cacheFile = new File("user/cache.dat");
        HashSet<String> set = new HashSet<>();
        StringBuilder allCaches = new StringBuilder();
        String eachLine;
        if (cacheFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(cacheFile))) {
                while ((eachLine = br.readLine()) != null) {
                    String[] each = semicolon.split(eachLine);
                    Collections.addAll(set, each);
                }
            } catch (IOException ignored) {

            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(cacheFile))) {
                for (String cache : set) {
                    allCaches.append(cache).append(";\n");
                }
                bw.write(allCaches.toString());
            } catch (IOException ignored) {

            }
        }
    }

    private void delCache(ArrayList<String> cache) {
        File cacheFile = new File("user/cache.dat");
        StringBuilder allCaches = new StringBuilder();
        String eachLine;
        if (cacheFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(cacheFile))) {
                while ((eachLine = br.readLine()) != null) {
                    String[] each = semicolon.split(eachLine);
                    for (String eachCache : each) {
                        if (!(cache.contains(eachCache))) {
                            allCaches.append(eachCache).append(";\n");
                        }
                    }
                }
            } catch (IOException ignored) {

            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(cacheFile))) {
                bw.write(allCaches.toString());
            } catch (IOException ignored) {

            }
        }
    }

    private void searchCache(String text, String searchCase) {
        String cacheResult;
        boolean isCacheRepeated = false;
        ArrayList<String> cachesToDel = new ArrayList<>();
        File cache = new File("user/cache.dat");
        if (cache.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(cache))) {
                while ((cacheResult = reader.readLine()) != null) {
                    String[] caches = semicolon.split(cacheResult);
                    for (String cach : caches) {
                        if (!(new File(cach).exists())) {
                            cachesToDel.add(cach);
                        } else {
                            String eachCacheName = getFileName(cach);
                            if (isSubString(eachCacheName, text)) {
                                if (!listResult.contains(cach)) {
                                    boolean fullMatched = eachCacheName.toLowerCase().equals(text.toLowerCase());
                                    switch (searchCase) {
                                        case "F":
                                            if (isFile(cach)) {
                                                listResult.add(cach);
                                            }
                                            break;
                                        case "D":
                                            if (isDirectory(cach)) {
                                                listResult.add(cach);
                                            }
                                            break;
                                        case "FULL":
                                            if (fullMatched) {
                                                listResult.add(cach);
                                            }
                                            break;
                                        case "DFULL":
                                            if (fullMatched && isDirectory(cach)) {
                                                listResult.add(cach);
                                            }
                                            break;
                                        case "FFULL":
                                            if (fullMatched && isFile(cach)) {
                                                listResult.add(cach);
                                            }
                                            break;
                                        default:
                                            listResult.add(cach);
                                    }
                                    if (listResult.size() > 100) {
                                        break;
                                    }
                                } else {
                                    isCacheRepeated = true;
                                }
                            }
                        }
                    }
                }
            } catch (IOException ignored) {

            }
        }
        delCache(cachesToDel);
        if (isCacheRepeated) {
            delCacheRepeated();
        }
    }

    private boolean isSubString(String srcText, String txt) {
        if (!txt.equals("")) {
            srcText = srcText.toLowerCase();
            txt = txt.toLowerCase();
            String[] keyWords = txt.split(";");
            for (String each : keyWords) {
                if (!srcText.contains(each)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void delRepeated() {
        LinkedHashSet<String> set = new LinkedHashSet<>(listResult);
        listResult.clear();
        listResult.addAll(set);
    }

    private void searchPriorityFolder(String text, String searchCase) {
        File path = new File(SettingsFrame.priorityFolder);
        boolean exist = path.exists();
        LinkedList<File> listRemain = new LinkedList<>();
        if (exist) {
            File[] files = path.listFiles();
            if (!(null == files || files.length == 0)) {
                for (File each : files) {
                    if (isSubString(getFileName(each.getAbsolutePath()), text)) {
                        switch (searchCase) {
                            case "F":
                                if (each.isFile()) {
                                    listResult.add(0, each.getAbsolutePath());
                                }
                                break;
                            case "D":
                                if (each.isDirectory()) {
                                    listResult.add(0, each.getAbsolutePath());
                                }
                                break;
                            case "FULL":
                                if (each.getName().equals(text)) {
                                    listResult.add(0, each.getAbsolutePath());
                                }
                                break;
                            case "DFULL":
                                if (each.getName().equals(text) && each.isDirectory()) {
                                    listResult.add(0, each.getAbsolutePath());
                                }
                                break;
                            case "FFULL":
                                if (each.getName().equals(text) && each.isFile()) {
                                    listResult.add(0, each.getAbsolutePath());
                                }
                                break;
                            default:
                                listResult.add(0, each.getAbsolutePath());
                        }
                    }
                    if (each.isDirectory()) {
                        listRemain.add(each);
                    }
                }
                while (!listRemain.isEmpty()) {
                    File remain = listRemain.pop();
                    File[] allFiles = remain.listFiles();
                    assert allFiles != null;
                    for (File each : allFiles) {
                        if (isSubString(getFileName(each.getAbsolutePath()), text)) {
                            switch (searchCase) {
                                case "F":
                                    if (each.isFile()) {
                                        listResult.add(0, each.getAbsolutePath());
                                    }
                                    break;
                                case "D":
                                    if (each.isDirectory()) {
                                        listResult.add(0, each.getAbsolutePath());
                                    }
                                    break;
                                case "FULL":
                                    if (each.getName().toLowerCase().equals(text.toLowerCase())) {
                                        listResult.add(0, each.getAbsolutePath());
                                    }
                                    break;
                                case "DFULL":
                                    if (each.getName().toLowerCase().equals(text.toLowerCase()) && each.isDirectory()) {
                                        listResult.add(0, each.getAbsolutePath());
                                    }
                                    break;
                                case "FFULL":
                                    if (each.getName().toLowerCase().equals(text.toLowerCase()) && each.isFile()) {
                                        listResult.add(0, each.getAbsolutePath());
                                    }
                                    break;
                            }
                        }
                        if (each.isDirectory()) {
                            listRemain.add(each);
                        }
                    }
                }
            }
        }
    }

    private void clearTextFieldText() {
        Runnable clear = () -> textField.setText(null);
        SwingUtilities.invokeLater(clear);
    }

    public void closedTodo() {
        Runnable todo = () -> {
            if (searchBar.isVisible()) {
                searchBar.setVisible(false);
            }
            CheckHotKey hotkeyListener = CheckHotKey.getInstance();
            hotkeyListener.setShowSearchBar(false);
            clearLabel();
            startTime = System.currentTimeMillis();//��������
            isUsing = false;
            labelCount = 0;
            listResult.clear();
            textField.setText(null);
            isOpenLastFolderPressed = false;
            isRunAsAdminPressed = false;
            isCopyPathPressed = false;
            try {
                searchWaiter.interrupt();
            } catch (NullPointerException ignored) {

            }
        };
        SwingUtilities.invokeLater(todo);
    }

    private String getParentPath(String path) {
        File f = new File(path);
        return f.getParent();
    }

    private boolean isFile(String text) {
        File file = new File(text);
        return file.isFile();
    }

    private boolean isDirectory(String text) {
        File file = new File(text);
        return file.isDirectory();
    }
}
