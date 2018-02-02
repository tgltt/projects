package com.uc.phantom.bra.ui.swing;


import com.uc.phantom.bra.framework.common.Consts;
import com.uc.phantom.bra.framework.common.ErrorCode;
import com.uc.phantom.bra.framework.filters.filter_text.BugReportFilters;
import com.uc.phantom.bra.framework.filters.filter_text.OtherLogFilters;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.StatisticInfo;
import com.uc.phantom.bra.framework.utils.Logger;
import com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.anr.ANRBase;
import com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.anr.ANREngine;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;


/**
 * Created by Administrator on 2017/4/21.
 */
public class Main implements Logger.LogInterface {

    private final static String TAG = "Main";

    public final static String COLOR_PLAIN_TEXT_SELECT_BACKGROUND = "plain text select background";
    public final static String COLOR_PLAIN_TEXT_NORMAL_BACKGROUND = "plain text normal background";
    public final static String COLOR_LOG_DATE_TIME = "date time";

    public final static int TAB_ID_BUGREPORT  = 1;
    public final static int TAB_ID_LOGCAT     = 2;
    public final static int TAB_ID_EVENT_LOG  = 3;
    public final static int TAB_ID_KERNEL_LOG = 4;
    public final static int TAB_ID_TRACES      = 5;

    public final static int TAB_ID_BUGREPORT_TREE = 6;
    public final static int TAB_ID_SPECIAL_TREE   = 7;

    public final static int TAB_INDEX_BUGREPORT  = 0;
    public final static int TAB_INDEX_LOGCAT     = 1;
    public final static int TAB_INDEX_EVENT_LOG  = 2;
    public final static int TAB_INDEX_KERNEL_LOG = 3;
    public final static int TAB_INDEX_TRACES      = 4;

    public final static int TAB_INDEX_BUGREPORT_TREE  = 0;
    public final static int TAB_INDEX_SPECIAL_TREE    = 1;

    public final static int CONTENT_INCREMENT = 2000;


    private JTextPane mContent;
    private JTree mFilterContent;
    private JPanel mRootPanel;
    private JToolBar mToolBar;
    private JTextPane mLogOutput;
    private JPanel mLogContainer;
    private JScrollPane mContentScrollPanel;
    private JPanel mContentContainer;
    private JPanel mSummrayContainer;
    private JScrollPane mSummaryScrollPanel;
    private JScrollPane mLogScrollPanel;
    private JPanel mToolBarPanel;
    private JPanel mSearchPanel;
    private JTextField mSearchText;
    private JButton mSearch;
    private JTabbedPane mLeftTabPane;
    private JTabbedPane mRightTabPane;
    private JPanel mSpecialPanel;
    private JScrollPane mSpecialScrollPane;
    private JTree mSpecial1;
    private JPanel mIndexPanel;
    private JTextPane mLogcatContent;
    private JTextPane mEventsContent;
    private JTextPane mKernelContent;
    private JScrollPane mAppLogCatScrollPanel;
    private JScrollPane mEventScrollPanel;
    private JScrollPane mKernelScrollPanel;
    private JTextPane mTraceContent;
    private JScrollPane mTraceScrollPanel;
    private JProgressBar mProgressBar;

    private JFrame mFrame;

    private Map<DefaultMutableTreeNode, TreeNodeTag> mTreeNodeChildrenMap;
    private Map<Integer, Map<DefaultMutableTreeNode, TreeNodeTag>> mChapterTreeNodeMap;

    private BugReportParseEngine.ParseResult mParseResult;
    private BugReportParseEngine.ParseResult mAppLocatParseResult;
    private BugReportParseEngine.ParseResult mLogEventParseResult;
    private BugReportParseEngine.ParseResult mKernelLogParseResult;
    private BugReportParseEngine.ParseResult mTracesResult;

    private MyAdjustmentListener mBugReportAdjustmentListener;
    private MyAdjustmentListener mAppLogcatAdjustmentListener;
    private MyAdjustmentListener mLogEventAdjustmentListener;
    private MyAdjustmentListener mKernelAdjustmentListener;
    private MyAdjustmentListener mTracesAdjustmentListener;

    private ScrollContentWatcher mBugReportScrollContentWatcher;
    private ScrollContentWatcher mAppLogcatScrollContentWatcher;
    private ScrollContentWatcher mLogEventScrollContentWatcher;
    private ScrollContentWatcher mKernelScrollContentWatcher;
    private ScrollContentWatcher mTracesScrollContentWatcher;

    private boolean mUserDragContentScroll;
    private boolean mSavingFilterInfoFlag;

    private MyTreeCellRenderer mMyTreeCellRenderer;

    private String mFilters = "com.android.browser";
    private File mLastSelectedFile;
    private Map<Integer, Integer> mTabID2TabIndexMap;
    private boolean mHasLoadExpertData;
    private boolean mRefreshExpertData;

    private static Map<String, Color> sLogColorMap;

    private List<ANRBase.ANRDiagnoseResult> mAnrDiagnoseResultList;

    private static boolean sDebugMode = false;

    static {
        Properties prop = new Properties();
        InputStream in = Object.class.getResourceAsStream("config/cfg.properties");
        try {
            prop.load(in);
            sDebugMode = Boolean.valueOf(prop.getProperty("debug"));
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        sLogColorMap = new HashMap<String, Color>();
        sLogColorMap.put(Logger.LOG_LEVEL_VERBOSE, Color.DARK_GRAY);
        sLogColorMap.put(Logger.LOG_LEVEL_DEBUG, Color.BLACK);
        sLogColorMap.put(Logger.LOG_LEVEL_INFO, new Color(0x22, 0x62, 0x31));
        sLogColorMap.put(Logger.LOG_LEVEL_ERROR, Color.RED);
        sLogColorMap.put(Logger.LOG_LEVEL_WARN, Color.ORANGE);
        sLogColorMap.put(Logger.LOG_LEVEL_FATAL, Color.MAGENTA);
        sLogColorMap.put(Logger.LOG_LEVEL_FATAL, Color.MAGENTA);
        sLogColorMap.put(COLOR_PLAIN_TEXT_NORMAL_BACKGROUND, new Color(0xff, 0xff, 0xff));
        sLogColorMap.put(COLOR_PLAIN_TEXT_SELECT_BACKGROUND, new Color(0x84, 0xd5, 0x84));
        sLogColorMap.put(COLOR_PLAIN_TEXT_SELECT_BACKGROUND, new Color(0x84, 0xd5, 0x84));
        sLogColorMap.put(COLOR_LOG_DATE_TIME, new Color(0x73, 0x0, 0x84));
    }

    public static void main(String[] args) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long minimumMemory = 500 * 1024 * 1024; // 500 MB.
        if (maxMemory < minimumMemory) {
            JOptionPane.showMessageDialog(null, "BugReport Analysis System requires minimum available memory 200MB.\n");
            return;
        }

//        Pattern pattern = Pattern.compile("(com.android.browser)");
//        Matcher matcher = pattern.matcher("  0.2% 10112/com.android.browser: 0.1% user + 0% kernel / faults: 3865 minor 4760 major");
//        while (matcher.find()) {
//            System.out.println(matcher.groupCount());
//            System.out.println(matcher.start());
//            System.out.println(matcher.end());
//            System.out.println(matcher.group());
//        }
//
////        System.out.println(Pattern.matches("\sANR\sin\s[\w+[.]]\s([\w+[.]/[\w+[.]]])", " ANR in com.android.browser (com.android.browser/com.uc.browser.InnerUCMobile)"));
////        System.out.println(Pattern.matches(Consts.PROBLEM_EXCEPTION_START, "02-06 21:22:12.750 24580 24580 W System.err: java.io.FileNotFoundException: /storage/emulated/0/autonavi/fd_tmp_file.txt (Permission denied)"));
//
//        pattern = Pattern.compile("(Java)(Python)");
//        String test = "123JavaPython456";
//        matcher = pattern.matcher(test);
//        matcher.find();
//        System.out.println("groupCount:" + matcher.groupCount());//返回2
//
//        System.out.println(matcher.group(1));//返回第一组匹配到的字符串"Java"，注意起始索引是1
//        System.out.println(matcher.start(1));//返回3，第一组起始索引
//        System.out.println(matcher.end(1));//返回7 第一组结束索引
//
//        System.out.println(matcher.group(2));//返回第二组匹配到的字符串"Python"
//        System.out.println(matcher.start(2));//返回7，第二组起始索引
//        System.out.println(matcher.end(2));//返回13 第二组结束索引

        Main mainEntrance = new Main();
        init(mainEntrance);

//        String s = "Hello, world!"; // 主串
//        String t = "rl"; // 模式串
//        System.out.println(s);
//        System.out.println(t);
//        int index  = StringUtils.kmp_indexOf(s, t);
//        System.out.println(s.substring(index, index + t.length())); // KMP匹配字符串
    }

    private static void init(Main entrance) {
        JFrame frame = new JFrame("Main");
        entrance.init(frame);
    }

    public Main() {

    }

    public void init(JFrame frame) {
        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);

        frame.setContentPane(mRootPanel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.pack();
        frame.setVisible(true);

        initOthers();

        //initLayout(frame);
        initToolBar();
        initUserUI();

        mFrame = frame;
    }

    private void initOthers() {
        Logger.getLogger(null).setLogInterface(this);
    }

    private void initLayout(JFrame frame) {
        Dimension dimen = frame.getContentPane().getMaximumSize();
        int maxWidth  = dimen.width;
        int maxHeight = dimen.height;

        mSummrayContainer.setPreferredSize(new Dimension((int)(maxWidth * 0.3 + 0.5) , (int)(maxHeight * 0.90 + 0.5)));
        mContentContainer.setPreferredSize(new Dimension((int)(maxWidth * 0.7 + 0.5), (int)(maxHeight * 0.90 + 0.5)));
        mLogContainer.setPreferredSize(new Dimension(maxWidth, (int)(maxHeight * 0.10 + 0.5)));

        mSummrayContainer.setMaximumSize(new Dimension((int)(maxWidth * 0.3 + 0.5) , (int)(maxHeight * 0.90 + 0.5)));
        mContentContainer.setMaximumSize(new Dimension((int)(maxWidth * 0.7 + 0.5), (int)(maxHeight * 0.90 + 0.5)));
        mLogContainer.setMaximumSize(new Dimension(maxWidth, (int)(maxHeight * 0.10 + 0.5)));

        mSummaryScrollPanel.setPreferredSize(new Dimension((int)(maxWidth * 0.3 + 0.5) , (int)(maxHeight * 0.90 + 0.5)));
        mContentScrollPanel.setPreferredSize(new Dimension((int)(maxWidth * 0.7 + 0.5), (int)(maxHeight * 0.90 + 0.5)));
        mLogScrollPanel.setPreferredSize(new Dimension(maxWidth, (int)(maxHeight * 0.10 + 0.5)));

        mSummaryScrollPanel.setMaximumSize(new Dimension((int)(maxWidth * 0.3 + 0.5) , (int)(maxHeight * 0.90 + 0.5)));
        mContentScrollPanel.setMaximumSize(new Dimension((int)(maxWidth * 0.7 + 0.5), (int)(maxHeight * 0.90 + 0.5)));
        mLogScrollPanel.setMaximumSize(new Dimension(maxWidth, (int)(maxHeight * 0.10 + 0.5)));



        mSummrayContainer.updateUI();
        mContentContainer.updateUI();
        mLogContainer.updateUI();
    }

    private void clearTree(JTree tree) {
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode nodeRoot = (DefaultMutableTreeNode) treeModel.getRoot();

        nodeRoot.removeAllChildren();

        tree.updateUI();
    }

    private void initUserUI() {
        initTrees();
        initContentTabs();
        initContentScrollbars();
    }

    private void initTrees() {
        initFilterContent();
        initSpecialContent();
    }

    private void initContentTabs() {
        mTabID2TabIndexMap = new HashMap<Integer, Integer>();
        mTabID2TabIndexMap.put(TAB_ID_BUGREPORT, TAB_INDEX_BUGREPORT);
        mTabID2TabIndexMap.put(TAB_ID_LOGCAT, TAB_INDEX_LOGCAT);
        mTabID2TabIndexMap.put(TAB_ID_EVENT_LOG, TAB_INDEX_EVENT_LOG);
        mTabID2TabIndexMap.put(TAB_ID_KERNEL_LOG, TAB_INDEX_KERNEL_LOG);
        mTabID2TabIndexMap.put(TAB_ID_TRACES, TAB_INDEX_TRACES);

        mTabID2TabIndexMap.put(TAB_ID_BUGREPORT_TREE, TAB_INDEX_BUGREPORT_TREE);
        mTabID2TabIndexMap.put(TAB_ID_SPECIAL_TREE, TAB_INDEX_SPECIAL_TREE);

        mProgressBar.setStringPainted(true);
    }

    private void initContentScrollbars() {
        initBugReportScrollbar();
        initAppLogcatScrollbar();
        initLogEventScrollbar();
        initKernelScrollbar();
        initTracesScrollbar();

        mProgressBar.setVisible(false);
    }

    private void initTracesScrollbar() {
        mTracesScrollContentWatcher = new ScrollContentWatcher();
        mTracesAdjustmentListener = new MyAdjustmentListener(mTracesScrollContentWatcher, mTracesResult, mTraceContent);

        initScrollbar(mTracesScrollContentWatcher, 0, CONTENT_INCREMENT, CONTENT_INCREMENT, -1, mTracesAdjustmentListener, mTraceScrollPanel);
    }

    private void initKernelScrollbar() {
        mKernelScrollContentWatcher = new ScrollContentWatcher();
        mKernelAdjustmentListener = new MyAdjustmentListener(mKernelScrollContentWatcher, mKernelLogParseResult, mKernelContent);

        initScrollbar(mKernelScrollContentWatcher, 0, CONTENT_INCREMENT, CONTENT_INCREMENT, -1, mKernelAdjustmentListener, mKernelScrollPanel);
    }

    private void initLogEventScrollbar() {
        mLogEventScrollContentWatcher = new ScrollContentWatcher();
        mLogEventAdjustmentListener = new MyAdjustmentListener(mLogEventScrollContentWatcher, mLogEventParseResult, mEventsContent);

        initScrollbar(mLogEventScrollContentWatcher, 0, CONTENT_INCREMENT, CONTENT_INCREMENT, -1, mLogEventAdjustmentListener, mEventScrollPanel);
    }

    private void initAppLogcatScrollbar() {
        mAppLogcatScrollContentWatcher = new ScrollContentWatcher();
        mAppLogcatAdjustmentListener = new MyAdjustmentListener(mAppLogcatScrollContentWatcher, mAppLocatParseResult, mLogcatContent);

        initScrollbar(mAppLogcatScrollContentWatcher, 0, CONTENT_INCREMENT, CONTENT_INCREMENT, -1, mAppLogcatAdjustmentListener, mAppLogCatScrollPanel);
    }

    private void initBugReportScrollbar() {
        mBugReportScrollContentWatcher = new ScrollContentWatcher();
        mBugReportAdjustmentListener = new MyAdjustmentListener(mBugReportScrollContentWatcher, mParseResult, mContent);

        initScrollbar(mBugReportScrollContentWatcher, 0, CONTENT_INCREMENT, 100, -1, mBugReportAdjustmentListener, mContentScrollPanel);
    }

    private void initScrollbar(ScrollContentWatcher scrollContentWatcher, int start, int end, int increment, int selectedLine, AdjustmentListener listener, JScrollPane scrollPane) {
        scrollContentWatcher.setStart(start);
        scrollContentWatcher.setEnd(end);
        scrollContentWatcher.setIncrement(increment);
        scrollContentWatcher.setSelectedLine(selectedLine);

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.addAdjustmentListener(listener);

        scrollBar.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (Main.this) {
                    mUserDragContentScroll = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                synchronized (Main.this) {
                    mUserDragContentScroll = false;
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private void initSpecialContent() {
        clearTree(mSpecial1);

        mSpecial1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                Object objUserObject = selectedNode.getUserObject();
                if (!(objUserObject instanceof TreeNodeTag))
                    return;

                TreeNodeTag tnt = (TreeNodeTag) objUserObject;
                if (tnt == null)
                    return;

                int tabId = tnt.getChapterId();
                int startLine = (int) tnt.getOrigStartLine();
                if (tabId == TAB_ID_LOGCAT) {
                    switchToTab(mRightTabPane, TAB_ID_LOGCAT);

                    mAppLogcatScrollContentWatcher.setStart(startLine);
                    mAppLogcatScrollContentWatcher.setEnd(startLine + CONTENT_INCREMENT);

                    mAppLogcatScrollContentWatcher.setSelectedLine(startLine);

                    loadContentPartially(mLogcatContent, mAppLocatParseResult, (int) mAppLogcatScrollContentWatcher.getStart(), (int) mAppLogcatScrollContentWatcher.getEnd(), (int) mAppLogcatScrollContentWatcher.getTotal(), (int) mAppLogcatScrollContentWatcher.getSelectedLine(), (int) mAppLogcatScrollContentWatcher.getStart(), (int) mAppLogcatScrollContentWatcher.getEnd(), startLine);

                    JScrollBar scrollBar = mAppLogCatScrollPanel.getVerticalScrollBar();
                    scrollBar.setValue(0);
                    scrollBar.updateUI();
                }
            }
        });
    }


    private void initFilterContent() {
        clearTree(mFilterContent);

        mFilterContent.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();

                TreeNodeTag tntTag  = mTreeNodeChildrenMap.get(selectNode);
                if (tntTag == null) {
                    Logger.getLogger(Main.this).log(TAG, Logger.LOG_LEVEL_ERROR, "" + selectNode + "'s TreeNodeTag is null.");
                    return;
                }

                if (tntTag.hasContent2Extract()) {
                    selectNode.removeAllChildren();

                    Integer chapterId = tntTag.getChapterId();

                    Map<DefaultMutableTreeNode, TreeNodeTag> mapSubTreeNodeTag = new HashMap<DefaultMutableTreeNode, TreeNodeTag>();
                    mChapterTreeNodeMap.put(chapterId, mapSubTreeNodeTag);

                    Map<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo> chapterFilterLineAtInfos = mParseResult.getChapterFilterLineAtInfo();
                    StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo chapterFilterLineAtInfo = chapterFilterLineAtInfos.get(chapterId);
                    // TODO: Need to extend, because java.util.List interface constraints.
                    int start = (int) chapterFilterLineAtInfo.getChapterStartLine();
                    int end   = (int) chapterFilterLineAtInfo.getChapterEndLine();
                    int filterStart = (int) chapterFilterLineAtInfo.getChapterFilterStartLine();
                    int filterEnd   = (int) chapterFilterLineAtInfo.getChapterFilterEndLine();

                    StatisticInfo.CacheLinesStatistic cacheLinesStatistic = mParseResult.getCacheLinesStatistic();
                    java.util.List<String> listLines = cacheLinesStatistic.getLinesList();
                    java.util.List<Long> listLineIndex = chapterFilterLineAtInfo.getExtraIncludeLinesList();

                    String line = listLines.get(start);
                    DefaultMutableTreeNode content = new DefaultMutableTreeNode();
                    selectNode.add(content);
                    content.setUserObject(getLineTag(start, line, chapterId));

                    int feature = chapterFilterLineAtInfo.getFeature();
                    if (feature == StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_BLOCK) {
                        for (int i = filterStart ; i <= filterEnd ; i++) {
                            line = listLines.get(i);
                            content = new DefaultMutableTreeNode();
                            selectNode.add(content);

                            content.setUserObject(getLineTag(i, line, chapterId));
                        }
                    } else if (feature == StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_SPARSE) {
                        for (int i = start ; i <= end ; i++) {
                            if (!listLineIndex.contains(Long.valueOf(i)))
                                continue;

                            line = listLines.get(i);
                            content = new DefaultMutableTreeNode();
                            selectNode.add(content);

                            content.setUserObject(getLineTag(i, line, chapterId));
                        }
                    } else { // Other case ignore now!!
                        for (int i = start ; i <= end ; i++) {
                            if (!listLineIndex.contains(Long.valueOf(i)))
                                continue;

                            line = listLines.get(i);
                            content = new DefaultMutableTreeNode();
                            selectNode.add(content);

                            content.setUserObject(getLineTag(i, line, chapterId));
                        }
                    }

                    line = listLines.get(end);
                    content = new DefaultMutableTreeNode();
                    content.setUserObject(getLineTag(end, line, chapterId));

                    selectNode.add(content);

                    tntTag.setHasContent2Extract(false);

                    mFilterContent.updateUI();
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        });

        mFilterContent.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                switchToTab(mRightTabPane, TAB_ID_BUGREPORT);

                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();

                Object objUserObject = selectedNode.getUserObject();
                if (!(objUserObject instanceof TreeNodeTag))
                    return;

                TreeNodeTag tnt = (TreeNodeTag) objUserObject;
                if (tnt == null)
                    return;

                int startLine = (int) tnt.getOrigStartLine();

                mBugReportScrollContentWatcher.setStart(startLine);
                mBugReportScrollContentWatcher.setEnd(startLine + CONTENT_INCREMENT);
                mBugReportScrollContentWatcher.setSelectedLine(tnt.getOrigStartLine());

                loadContentPartially(mContent, mParseResult, (int) mBugReportScrollContentWatcher.getStart(), (int) mBugReportScrollContentWatcher.getEnd(), (int) mBugReportScrollContentWatcher.getTotal(), (int) mBugReportScrollContentWatcher.getSelectedLine(), (int) mBugReportScrollContentWatcher.getStart(), (int) mBugReportScrollContentWatcher.getEnd(), startLine);

                JScrollBar scrollBar = mContentScrollPanel.getVerticalScrollBar();
                scrollBar.setValue(0);
                scrollBar.updateUI();
            }
        });

        mMyTreeCellRenderer = new MyTreeCellRenderer(mFilterContent.getCellRenderer());
        mFilterContent.setCellRenderer(mMyTreeCellRenderer);


    }

    private TreeNodeTag getLineTag(long lineNo, String line, Integer chapterId) {
        TreeNodeTag subTnt = new TreeNodeTag();
        subTnt.setLine(line);
        subTnt.setLeafNode(true);
        subTnt.setOrigStartLine(lineNo);
        subTnt.setOrigEndLine(lineNo);
        subTnt.setChapterId(chapterId);
        subTnt.setHasContent2Extract(false);

        return subTnt;
    }

    private void initToolBar() {
        File fileRoot;

        if (sDebugMode) {
            fileRoot = new File("BugReportAnalysis", "/res/images");
        } else {
            fileRoot = new File("res/images");
        }

        System.out.println(fileRoot.getAbsoluteFile());
        Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "fileRoot=" + fileRoot.getAbsolutePath());

        JButton btnOpen = new JButton(new ImageIcon(new File(fileRoot, "open.png").getAbsolutePath()));
        btnOpen.setToolTipText("Open");
        btnOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAnalysis(true);
            }
        });

        mToolBar.add(btnOpen);

        JButton btnSave = new JButton(new ImageIcon(new File(fileRoot, "save.png").getAbsolutePath()));
        btnSave.setToolTipText("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mParseResult == null || !mParseResult.isValid()) {
                    JOptionPane.showMessageDialog(null, "No valid data.");
                    return;
                }
                saveFilterBugReportInfo();
            }
        });

        mToolBar.add(btnSave);

        JButton btnExit = new JButton(new ImageIcon(new File(fileRoot, "exit.png").getAbsolutePath()));
        btnExit.setToolTipText("Exit");

        mToolBar.add(btnExit);

        JButton btnBugReport = new JButton(new ImageIcon(new File(fileRoot, "bugreport.png").getAbsolutePath()));
        btnBugReport.setToolTipText("BugReport Display/Hide");
        btnBugReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startExpertAnalysis();
            }
        });

        mToolBar.add(btnBugReport);

        JButton btnCatalog = new JButton(new ImageIcon(new File(fileRoot, "catalog.png").getAbsolutePath()));
        btnCatalog.setToolTipText("BugReport Catalog");

        mToolBar.add(btnCatalog);

        mSearchText.setText(" " + mFilters);

        mSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = mSearchText.getText();
                if (searchText == null || searchText.equals("")) {
                    JOptionPane.showMessageDialog(null, "Input search text.");
                    return;
                }

                boolean reSelectFile = false;
                if (mLastSelectedFile == null || !mLastSelectedFile.exists()) {
                    reSelectFile = true;
                }

                startAnalysis(reSelectFile);
            }
        });
    }

    private void saveFilterBugReportInfo() {
        mSearch.setEnabled(false);
        mSavingFilterInfoFlag = true;

        File fileSaveDir = selectBugFile(JFileChooser.DIRECTORIES_ONLY);
        File fileSave = new File(fileSaveDir, Consts.BUGREPORT_FILENAME);

        BufferedWriter bWriter = null;
        try {
            bWriter = new BufferedWriter(new FileWriter(fileSave));

            bWriter.write("Filer:" + mFilters + "\r\n\r\n");

            Map<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo> chapterFilterLineAtInfos = mParseResult.getChapterFilterLineAtInfo();
            StatisticInfo.CacheLinesStatistic cacheLinesStatistic = mParseResult.getCacheLinesStatistic();
            java.util.List<String> listLines = cacheLinesStatistic.getLinesList();

            final Set<Integer> setKeys = chapterFilterLineAtInfos.keySet();
            for (Integer key : setKeys) {
                StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo chapterFilterLineAtInfo = mParseResult.getChapterFilterLineAtInfo().get(key);
                if (chapterFilterLineAtInfo == null)
                    continue;

                int start = (int) chapterFilterLineAtInfo.getChapterStartLine();
                int end   = (int) chapterFilterLineAtInfo.getChapterEndLine();
                int filterStart = (int) chapterFilterLineAtInfo.getChapterFilterStartLine();
                int filterEnd   = (int) chapterFilterLineAtInfo.getChapterFilterEndLine();

                java.util.List<Long> listLineIndex = chapterFilterLineAtInfo.getExtraIncludeLinesList();

                String line = listLines.get(start);
                bWriter.write(line + "\r\n");

                int feature = chapterFilterLineAtInfo.getFeature();
                if (feature == StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_BLOCK) {
                    for (int i = filterStart ; i <= filterEnd ; i++) {
                        line = listLines.get(i);
                        bWriter.write(line + "\r\n");
                    }
                } else if (feature == StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_SPARSE) {
                    for (int i = start ; i <= end ; i++) {
                        if (!listLineIndex.contains(Long.valueOf(i)))
                            continue;

                        line = listLines.get(i);
                        bWriter.write(line + "\r\n");
                    }
                } else { // Other case ignore now!!
                    if (key == StatisticInfo.ChapterMapping.CHAPTER_LOGCAT) {
                        StatisticInfo.PlainProblems problems = mParseResult.getPlainProblems();
                        bWriter.write("---------------------- Problems ----------------------\r\n");
                        List<StatisticInfo.PlainProblems.Problem> listProblems = problems.getExceptionList();
                        for (StatisticInfo.PlainProblems.Problem problem : listProblems) {
                            bWriter.write(" ** Exception\r\n");
                            StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo cfli = problem.getChapterFilterLineAtInfo();
                            start = (int) cfli.getChapterStartLine();
                            end   = (int) cfli.getChapterEndLine();
                            for (int i = start ; i <= end ; i++) {
                                bWriter.write(listLines.get(i) + "\r\n");
                            }
                            bWriter.write("\r\n");
                        }

                        listProblems = problems.getFatalList();
                        for (StatisticInfo.PlainProblems.Problem problem : listProblems) {
                            bWriter.write(" ** Fatal\r\n");
                            StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo cfli = problem.getChapterFilterLineAtInfo();
                            start = (int) cfli.getChapterStartLine();
                            end   = (int) cfli.getChapterEndLine();
                            for (int i = start ; i <= end ; i++) {
                                bWriter.write(listLines.get(i) + "\r\n");
                            }
                            bWriter.write("\r\n");
                        }

                        listProblems = problems.getBuildFingerprintList();
                        for (StatisticInfo.PlainProblems.Problem problem : listProblems) {
                            bWriter.write(" ** Build Fingerprint\r\n");
                            StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo cfli = problem.getChapterFilterLineAtInfo();
                            start = (int) cfli.getChapterStartLine();
                            end   = (int) cfli.getChapterEndLine();
                            for (int i = start ; i <= end ; i++) {
                                bWriter.write(listLines.get(i) + "\r\n");
                            }
                            bWriter.write("\r\n");
                        }

                        listProblems = problems.getDexOptList();
                        for (StatisticInfo.PlainProblems.Problem problem : listProblems) {
                            bWriter.write(" ** DexOpt\r\n");
                            StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo cfli = problem.getChapterFilterLineAtInfo();
                            start = (int) cfli.getChapterStartLine();
                            end   = (int) cfli.getChapterEndLine();
                            for (int i = start ; i <= end ; i++) {
                                bWriter.write(listLines.get(i) + "\r\n");
                            }
                            bWriter.write("\r\n");
                        }

                        bWriter.write("---------------------- End Problems ----------------------\r\n");
                    }
                }

                line = listLines.get(end);
                bWriter.write(line + "\r\n");
            }

            bWriter.flush();

            JOptionPane.showMessageDialog(null, fileSave.getAbsoluteFile() + " saved successfully.");
        } catch (IOException ioex) {
            Logger.getLogger(null).log(TAG, ioex.getMessage(), ioex);
            JOptionPane.showMessageDialog(null, fileSave.getAbsoluteFile() + " saved failed.\nReason: \n" + ioex);
        } finally {
            if (bWriter != null) {
                try {
                    bWriter.close();
                } catch (IOException ioex1) {
                    Logger.getLogger(null).log(TAG, "Close save bugreport stream failed.", ioex1);
                }

                bWriter = null;
            }

            mSearch.setEnabled(true);
            mSavingFilterInfoFlag = false;
        }

    }

    private File selectBugFile(int flag) {
        File fileCur = new File(".");
        JFileChooser fileChooser = new JFileChooser(fileCur);

        fileChooser.setFileSelectionMode(flag);
        fileChooser.showOpenDialog(null);

        return fileChooser.getSelectedFile();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenu menuExpert = new JMenu("Expert");
        JMenu menuHelp = new JMenu("Help");

        // File Menu Items.
        JMenuItem miFileOpen = new JMenuItem("Open");
        JMenuItem miFileSave = new JMenuItem("Save");
        JMenuItem miFileExit = new JMenuItem("Exit");

        miFileOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mSavingFilterInfoFlag) {
                    JOptionPane.showMessageDialog(null, "Saving data, try later.");
                    return;
                }

                startAnalysis(true);
            }
        });

        miFileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mParseResult == null || !mParseResult.isValid()) {
                    JOptionPane.showMessageDialog(null, "No valid data.");
                    return;
                }
                saveFilterBugReportInfo();
            }
        });

        miFileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuFile.add(miFileOpen);
        menuFile.add(miFileSave);
        menuFile.add(miFileExit);


        JMenuItem miFileExpertAnalysis = new JMenuItem("Expert Analysis");
        miFileExpertAnalysis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startExpertAnalysis();
            }
        });

        menuExpert.add(miFileExpertAnalysis);
        // File Menu Items.
        JMenuItem miHelpAbout = new JMenuItem("About");

        miHelpAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "BugReport Analysis beta1", "About", JOptionPane.YES_OPTION);
            }
        });

        menuHelp.add(miHelpAbout);

        menuBar.add(menuFile);
        menuBar.add(menuExpert);
        menuBar.add(menuHelp);

        return menuBar;
    }

    private void startExpertAnalysis() {
        Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "startExpertAnalysis");

        switchToTab(mLeftTabPane, TAB_ID_SPECIAL_TREE);

        if (mAnrDiagnoseResultList == null) {
            mAnrDiagnoseResultList = new ArrayList<ANRBase.ANRDiagnoseResult>();
        } else {
            mAnrDiagnoseResultList.clear();
        }

        Thread expertAnalysisThread = new Thread() {
            @Override
            public void run() {
                ANREngine anrEngine = ANREngine.getInstance();
                anrEngine.addOnDiagnoseCompelteListener(new ANREngine.OnDiagnoseCompelteListener() {
                    @Override
                    public void onDiagnoseComplete(ANRBase.ANRDiagnoseResult listener) {
                        System.out.println("onDiagnoseComplete");
                        updateAnalyzeResultUI(listener);
                    }
                });
                anrEngine.recognize(mAppLocatParseResult.getCacheLinesStatistic().getLinesList(),
                                    mTracesResult.getCacheLinesStatistic().getLinesList());

            }
        };

        expertAnalysisThread.start();
//        try {
//            expertAnalysisThread.join();
//        } catch (InterruptedException itex) {
//            Logger.getLogger(null).log(TAG, itex.getLocalizedMessage(), itex);
//            itex.printStackTrace();
//        }

    }

    private synchronized void updateAnalyzeResultUI(ANRBase.ANRDiagnoseResult listener) {
        //mFrame.setEnabled(false);
        mSpecial1.getModel();

        mAppLogcatScrollContentWatcher.setTotal(mAppLocatParseResult.getCacheLinesStatistic().getTotalLine());
        mLogEventScrollContentWatcher.setTotal(mLogEventParseResult.getCacheLinesStatistic().getTotalLine());
        mKernelScrollContentWatcher.setTotal(mKernelLogParseResult.getCacheLinesStatistic().getTotalLine());
        mTracesScrollContentWatcher.setTotal(mTracesResult.getCacheLinesStatistic().getTotalLine());

        ANRBase.ANRParsedResult anrParsedResult = listener.getANRParsedResult();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ANR Analysis");

        DefaultTreeModel treeModel = (DefaultTreeModel) mSpecial1.getModel();
        DefaultMutableTreeNode nodeRoot = (DefaultMutableTreeNode) treeModel.getRoot();

        nodeRoot.removeAllChildren();
        nodeRoot.add(root);

        mAnrDiagnoseResultList.add(listener);
        // ANR node root.
        TreeNodeTag tnt = new TreeNodeTag();
        tnt.setLine("ANR");
        DefaultMutableTreeNode anrNodeRoot = new DefaultMutableTreeNode(tnt);

        root.add(anrNodeRoot);

        Map<String, Integer> mapLines = listener.getANRParsedResult().getLine2LineNoMap();
        // Pid.
        tnt = getLineTag(mapLines.get(ANRBase.ANRParsedResult.KEY_PID), "Pid: " + listener.getANRParsedResult().getPid(), TAB_ID_LOGCAT);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(tnt);

        anrNodeRoot.add(node);
        // Reason.
        tnt = getLineTag(mapLines.get(ANRBase.ANRParsedResult.KEY_REASON), "Reason: " + listener.getANRParsedResult().getReason(), TAB_ID_LOGCAT);
        node = new DefaultMutableTreeNode(tnt);

        anrNodeRoot.add(node);
        // Source.
        tnt = getLineTag(mapLines.get(ANRBase.ANRParsedResult.KEY_SOURCE), "Source: " + listener.getANRParsedResult().getSource(), TAB_ID_LOGCAT);
        node = new DefaultMutableTreeNode(tnt);

        anrNodeRoot.add(node);
        // Longest time consuming.
        ANRBase.ANRItem anrItem = listener.getMostExaustTimeItem();
        tnt = new TreeNodeTag();
        tnt.setLine("Longest Time Consuming Process");
        DefaultMutableTreeNode ltcNodeRoot = new DefaultMutableTreeNode(tnt);

        anrNodeRoot.add(ltcNodeRoot);

        tnt = new TreeNodeTag();
        tnt.setLine(String.format("(%s/%s): total: %.2f%% [user: %.2f%%, kernel: %.2f%%, iowait: %.2f%%, softirq: %.2f%%]",
                    anrItem.getThreadId(),
                    anrItem.getThreadName(),
                    anrItem.getCpuPercentTotal(),
                    anrItem.getCpuPercentUser(),
                    anrItem.getCpuPercentKernel(),
                    anrItem.getCpuPercentIOWait(),
                    anrItem.getCpuPercentSoftIrq()));

        DefaultMutableTreeNode threadNodeRoot = new DefaultMutableTreeNode(tnt);
        ltcNodeRoot.add(threadNodeRoot);

        final List<ANRBase.ANRItem> listAnrs = anrItem.getThreadsCpuFrequencies();
        for (ANRBase.ANRItem item : listAnrs) {
            tnt = new TreeNodeTag();
            tnt.setLine(String.format("(%s/%s): total:%.2f%% [user:%.2f%%, kernel:%.2f%%, iowait:%.2f%%, softirq:%.2f%%]",
                        item.getThreadId(),
                        item.getThreadName(),
                        item.getCpuPercentTotal(),
                        item.getCpuPercentUser(),
                        item.getCpuPercentKernel(),
                        item.getCpuPercentIOWait(),
                        item.getCpuPercentSoftIrq()));
            node = new DefaultMutableTreeNode(tnt);

            threadNodeRoot.add(node);
        }

        List<String> tracesLines = mTracesResult.getCacheLinesStatistic().getLinesList();
        // Main thread status.
        ANRBase.ANRParsedResult.Traces.ThreadTraces threadTraces = listener.getMainThreadStatus();

        tnt = new TreeNodeTag();
        tnt.setLine(String.format("Main Thread Status(tid=%s, sysTid=%s)", threadTraces.getThreadId(), threadTraces.getSysTid()));
        DefaultMutableTreeNode mainThreadNodeRoot = new DefaultMutableTreeNode(tnt);

        anrNodeRoot.add(mainThreadNodeRoot);

        tnt = new TreeNodeTag();
        tnt.setLine("Status: " + threadTraces.getStatus());

        node = new DefaultMutableTreeNode(tnt);
        mainThreadNodeRoot.add(node);

        tnt = new TreeNodeTag();
        Integer integerLineNo = threadTraces.getKernelStartLineNo();
        int lineNo = integerLineNo == null ? -1 : integerLineNo.intValue();
        tnt.setOrigStartLine(lineNo);
        tnt.setOrigEndLine(lineNo);
        tnt.setLine("Kernel: " + (lineNo >= 0 ? tracesLines.get(lineNo) : ""));

        node = new DefaultMutableTreeNode(tnt);
        mainThreadNodeRoot.add(node);

        tnt = new TreeNodeTag();
        integerLineNo = threadTraces.getNativeStartLineNo();
        lineNo = integerLineNo == null ? -1 : integerLineNo.intValue();
        tnt.setOrigStartLine(lineNo);
        tnt.setOrigEndLine(lineNo);
        tnt.setLine("Native: " + (lineNo >= 0 ? tracesLines.get(lineNo) : ""));

        node = new DefaultMutableTreeNode(tnt);
        mainThreadNodeRoot.add(node);

        tnt = new TreeNodeTag();
        integerLineNo = threadTraces.getJavaStartLineNo();
        lineNo = integerLineNo == null ? -1 : integerLineNo.intValue();
        tnt.setOrigStartLine(lineNo);
        tnt.setOrigEndLine(lineNo);
        tnt.setLine("Java: " + (lineNo >= 0 ? tracesLines.get(lineNo) : ""));

        node = new DefaultMutableTreeNode(tnt);
        mainThreadNodeRoot.add(node);

        ANRBase.ANRParsedResult.ReasonDetail rd = anrParsedResult.getReasonDetail();

        tnt = new TreeNodeTag();
        tnt.setLine("Logcat Lines: ");
        DefaultMutableTreeNode nodeLogcat = new DefaultMutableTreeNode(tnt);
        anrNodeRoot.add(nodeLogcat);

        List<String> logcatLines = mAppLocatParseResult.getCacheLinesStatistic().getLinesList();
        final List<Integer> logcatLineList = rd.getLogcatLineNoList();
        for (Integer logcatLineNo : logcatLineList) {
            tnt = new TreeNodeTag();
            tnt.setOrigStartLine(logcatLineNo);
            tnt.setOrigEndLine(logcatLineNo);
            tnt.setLine(logcatLines.get(logcatLineNo));

            node = new DefaultMutableTreeNode(tnt);
            nodeLogcat.add(node);
        }

        tnt = new TreeNodeTag();
        tnt.setLine("Traces Lines: ");
        DefaultMutableTreeNode nodeTraces = new DefaultMutableTreeNode(tnt);
        anrNodeRoot.add(nodeTraces);

        final List<Integer> tracesLineList = rd.getTracesLineNoList();
        for (Integer tracesLineNo : tracesLineList) {
            tnt = new TreeNodeTag();
            tnt.setOrigStartLine(tracesLineNo);
            tnt.setOrigEndLine(tracesLineNo);
            tnt.setLine(tracesLines.get(tracesLineNo));

            node = new DefaultMutableTreeNode(tnt);
            nodeTraces.add(node);
        }

        StringBuilder sbDiagnoseSection = new StringBuilder();
        final List<String> diagnoseSection = listener.getDiagnoseResult();
        for (String diagnose : diagnoseSection) {
            sbDiagnoseSection.append(diagnose).append("\r\n");
        }


        tnt = new TreeNodeTag();
        tnt.setLine("Dianogse: " + sbDiagnoseSection); //rd.getDesc()
        node = new DefaultMutableTreeNode(tnt);
        anrNodeRoot.add(node);

        mSpecial1.updateUI();
        mFrame.setEnabled(true);
    }

    private void startAnalysis(boolean reSelectFile) {
        if (reSelectFile) {
            File fileSelected = selectBugFile(JFileChooser.FILES_AND_DIRECTORIES);
            if (fileSelected.isDirectory()) {
                Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "Directory:" + fileSelected.getAbsolutePath());
                fileSelected = new File(fileSelected, Consts.BUGREPORT_FILENAME);
            } else if(fileSelected.isFile()) {
                Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "File:" + fileSelected.getAbsolutePath());
            }

            mLastSelectedFile = fileSelected;

            if (fileSelected == null || !fileSelected.exists()) {
                JOptionPane.showMessageDialog(null, String.format("%s not found.", fileSelected.getAbsoluteFile()));
                return;
            }
        }

        String searchText = mSearchText.getText();
        if (searchText == null || searchText.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Input search text.");
            return;
        }

        releaseParsedResult();

        mFilters = searchText.trim();
        mMyTreeCellRenderer.setKey(mFilters);

        Thread normalAnalysisThread = new Thread() {
            @Override
            public void run() {
                //mFrame.setEnabled(false);
                mProgressBar.setVisible(true);
                mProgressBar.setValue(0);

                File fileSelected = mLastSelectedFile;
                StringBuilder sbLog = new StringBuilder();

                long curTime = System.currentTimeMillis();
                int errCode = BugReportFilters.filterTextAndProblem4LogFile(fileSelected, mFilters, mParseResult);
                curTime = System.currentTimeMillis() - curTime;
                Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, String.format("Parsed BugReport finished, errCode=%d, consuming time: %.2f seconds", errCode, curTime / 1000.0));

                if (errCode != ErrorCode.PARSER_RESULT_OK) {
                    String errMsg = String.format("BugReport in %s parsing failed, errCode=%d", fileSelected.getParent(), errCode);
                    JOptionPane.showMessageDialog(null, errMsg, "Error", JOptionPane.YES_OPTION);
                    sbLog.append(errMsg).append("\n");
                } else {
                    sbLog.append(String.format("BugReport in %s load successfully.\n", fileSelected.getParent()));
                    updateParsedResultUI(mLastSelectedFile, mParseResult);
                }

                mProgressBar.setValue(50);

                if (!mHasLoadExpertData) {
                    if (fileSelected.isFile()) {
                        fileSelected = fileSelected.getParentFile();
                    }
                    final File fileDir = new File(fileSelected, Consts.ANDROID_DETAIL_LOG_DIR);

                    String searchText = mSearchText.getText();
                    if (searchText == null || searchText.trim().equals("")) {
//                    JOptionPane.showMessageDialog(null, "Input search text.");
//                    return;
                    }
                    mFilters = searchText.trim();

                    int errCode4Logcat = OtherLogFilters.filterText4AppLogCat(fileDir, mFilters, 0, mAppLocatParseResult);
                    if (errCode4Logcat != ErrorCode.PARSER_RESULT_OK) {
                        //JOptionPane.showMessageDialog(null, "Parse " + fileDir.getAbsolutePath() + " failed, errCode=" + errCode4Logcat, "Error", JOptionPane.YES_OPTION);
                        sbLog.append(String.format("AppLogcat in %s load failed, errCode=%d.\n", fileDir.getAbsoluteFile(), errCode4Logcat));
                    } else {
                        sbLog.append(String.format("AppLogcat in %s load successfully.\n", fileDir.getAbsoluteFile()));

                        switchToTab(mRightTabPane, TAB_ID_LOGCAT);

                        mAppLogcatAdjustmentListener.setParseResult(mAppLocatParseResult);

                        long total = mAppLocatParseResult.getCacheLinesStatistic().getTotalLine();
                        loadContentPartially(mLogcatContent, mAppLocatParseResult, 0,  2000, (int) total,-1, 0, (int)total- 1, 0);
                    }

                    mProgressBar.setValue(75);

                    int errCode4Event = OtherLogFilters.filterText4EventLog(fileDir, mFilters, 0, mLogEventParseResult);
                    if (errCode4Event != ErrorCode.PARSER_RESULT_OK) {
                        //JOptionPane.showMessageDialog(null, "Parse " + fileDir.getAbsolutePath() + " failed, errCode=" + errCode4Event, "Error", JOptionPane.YES_OPTION);
                        sbLog.append(String.format("LogEvent in %s load failed, errCode=%d.\n", fileDir.getAbsoluteFile(), errCode4Event));
                    } else {
                        sbLog.append(String.format("LogEvent in %s load successfully.\n", fileDir.getAbsoluteFile()));

                        switchToTab(mRightTabPane, TAB_ID_EVENT_LOG);

                        mLogEventAdjustmentListener.setParseResult(mLogEventParseResult);

                        long total = mLogEventParseResult.getCacheLinesStatistic().getTotalLine();
                        loadContentPartially(mEventsContent, mLogEventParseResult, 0,  2000, (int) total,-1, 0, (int)total- 1, 0);
                    }

                    mProgressBar.setValue(85);

                    int errCode4Kernel = OtherLogFilters.filterText4KernelLog(fileDir, mFilters, 0, mKernelLogParseResult);
                    if (errCode4Kernel != ErrorCode.PARSER_RESULT_OK) {
                        //JOptionPane.showMessageDialog(null, "Parse " + fileDir.getAbsolutePath() + " failed, errCode=" + errCode4Kernel, "Error", JOptionPane.YES_OPTION);
                        sbLog.append(String.format("KMsgLog in %s load failed, errCode=%d.\n", fileDir.getAbsoluteFile(), errCode4Kernel));
                    } else {
                        sbLog.append(String.format("KMsgLog in %s load successfully.\n", fileDir.getAbsoluteFile()));

                        switchToTab(mRightTabPane, TAB_ID_KERNEL_LOG);

                        mKernelAdjustmentListener.setParseResult(mKernelLogParseResult);

                        long total = mKernelLogParseResult.getCacheLinesStatistic().getTotalLine();
                        loadContentPartially(mKernelContent, mKernelLogParseResult, 0,  2000, (int) total,-1, 0, (int)total- 1, 0);
                    }

                    mProgressBar.setValue(95);

                    int errCode4Traces = OtherLogFilters.filterText4Traces(fileDir, mFilters, 0, mTracesResult);
                    if (errCode4Traces != ErrorCode.PARSER_RESULT_OK) {
                        //JOptionPane.showMessageDialog(null, "Parse " + fileDir.getAbsolutePath() + " failed, errCode=" + errCode4Traces, "Error", JOptionPane.YES_OPTION);
                        sbLog.append(String.format("Traces in %s load failed, errCode=%d.\n", fileDir.getAbsoluteFile(), errCode4Traces));
                    } else {
                        sbLog.append(String.format("Traces in %s load successfully.\n", fileDir.getAbsoluteFile()));
                        switchToTab(mRightTabPane, TAB_ID_TRACES);

                        mTracesAdjustmentListener.setParseResult(mTracesResult);

                        long total = mTracesResult.getCacheLinesStatistic().getTotalLine();
                        loadContentPartially(mTraceContent, mTracesResult, 0,  2000, (int) total,-1, 0,  (int)total- 1, 0);
                    }

                    mProgressBar.setValue(100);

                    mHasLoadExpertData = true;
                    JOptionPane.showMessageDialog(null, sbLog.toString());
                }
                //mFrame.setEnabled(true);
                mProgressBar.setVisible(false);
            }
        };

        normalAnalysisThread.start();
//        try {
//            normalAnalysisThread.join();
//        } catch (InterruptedException itex) {
//            Logger.getLogger(null).log(TAG, itex.getLocalizedMessage(), itex);
//            itex.printStackTrace();
//        }
    }

    /**
     * Update UI safely.
     * @param parseResult
     */
    private synchronized void updateParsedResultUI(File bugReport, BugReportParseEngine.ParseResult parseResult) {
        //mFrame.setEnabled(false);

        Logger.getLogger(this).log(TAG, Logger.LOG_LEVEL_INFO, "updateParsedResultUI");

        Map<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo> chapterFilterLineAtInfos = parseResult.getChapterFilterLineAtInfo();

        if (mTreeNodeChildrenMap != null) {
            mTreeNodeChildrenMap.clear();
        } else {
            mTreeNodeChildrenMap = new HashMap<DefaultMutableTreeNode, TreeNodeTag>();
        }

        if (mChapterTreeNodeMap != null) {
            mChapterTreeNodeMap.clear();
        } else {
            mChapterTreeNodeMap  = new HashMap<Integer, Map<DefaultMutableTreeNode, TreeNodeTag>>();
        }

        final Map<DefaultMutableTreeNode, TreeNodeTag> mapTreeNodeChildren = mTreeNodeChildrenMap;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(bugReport.getAbsolutePath());

        final Set<Integer> setKeys = chapterFilterLineAtInfos.keySet();
        for (Integer key : setKeys) {
            StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo chapterFilterLineAtInfo = parseResult.getChapterFilterLineAtInfo().get(key);
            if (chapterFilterLineAtInfo == null)
                continue;

            TreeNodeTag tnt = new TreeNodeTag();
            tnt.setChapterId(key);
            tnt.setLine(StatisticInfo.ChapterMapping.getChapterTitle(key));
            tnt.setHasContent2Extract(true);
            tnt.setLeafNode(false);
            tnt.setOrigStartLine(chapterFilterLineAtInfo.getChapterStartLine());
            tnt.setOrigEndLine(chapterFilterLineAtInfo.getChapterEndLine());

            DefaultMutableTreeNode nodeChapter = new DefaultMutableTreeNode(tnt);
            mapTreeNodeChildren.put(nodeChapter, tnt);

            DefaultMutableTreeNode fakeSubNode = new DefaultMutableTreeNode("");
            nodeChapter.add(fakeSubNode);

            root.add(nodeChapter);
        }

        root.add(getProblemTreeNode(parseResult));

        DefaultTreeModel treeModel = (DefaultTreeModel)  mFilterContent.getModel();
        DefaultMutableTreeNode nodeRoot = (DefaultMutableTreeNode) treeModel.getRoot();

        nodeRoot.removeAllChildren();
        nodeRoot.add(root);

        mFilterContent.updateUI();

        mBugReportAdjustmentListener.setParseResult(mParseResult);

        JScrollBar scrollBar = mContentScrollPanel.getVerticalScrollBar();
        scrollBar.setValues(0, 100, 0, (int) parseResult.getCacheLinesStatistic().getTotalLine());
        mBugReportScrollContentWatcher.setTotal(parseResult.getCacheLinesStatistic().getTotalLine());

        loadContentPartially(mContent, mParseResult, (int) mBugReportScrollContentWatcher.getStart(), (int) mBugReportScrollContentWatcher.getEnd(), (int) mBugReportScrollContentWatcher.getTotal(), (int) mBugReportScrollContentWatcher.getSelectedLine(), (int) mBugReportScrollContentWatcher.getStart(), (int) mBugReportScrollContentWatcher.getEnd(), (int) mBugReportScrollContentWatcher.getStart());

        //mFrame.setEnabled(true);
    }

    private void releaseParsedResult() {
        if (mParseResult != null) {
            mParseResult.release();
            mParseResult = null;
        }

        if (mAppLocatParseResult != null) {
            mAppLocatParseResult.release();
            mAppLocatParseResult = null;
        }

        if (mLogEventParseResult != null) {
            mLogEventParseResult.release();
            mLogEventParseResult = null;
        }

        if (mKernelLogParseResult != null) {
            mKernelLogParseResult.release();
            mKernelLogParseResult = null;
        }

        if (mTracesResult != null) {
            mTracesResult.release();
            mTracesResult = null;
        }

        mHasLoadExpertData = false;

        mAppLocatParseResult  = new BugReportParseEngine.ParseResult();
        mLogEventParseResult  = new BugReportParseEngine.ParseResult();
        mKernelLogParseResult = new BugReportParseEngine.ParseResult();
        mTracesResult          = new BugReportParseEngine.ParseResult();

        mParseResult = new BugReportParseEngine.ParseResult();

        switchToTab(mLeftTabPane, TAB_ID_BUGREPORT_TREE);
        switchToTab(mRightTabPane, TAB_ID_BUGREPORT);

        clearTextPane(mContent);
        clearTextPane(mLogcatContent);
        clearTextPane(mEventsContent);
        clearTextPane(mKernelContent);
        clearTextPane(mTraceContent);
    }

    private void clearTextPane(JTextPane content) {
        StyledDocument styledDocument = content.getStyledDocument();
        try {
            styledDocument.remove(0, (int) styledDocument.getLength());
        } catch (BadLocationException blex) {
            Logger.getLogger(this).log(TAG, blex.getMessage(), blex);
            blex.printStackTrace();
        }

        content.updateUI();
    }

    private DefaultMutableTreeNode getProblemTreeNode(BugReportParseEngine.ParseResult parseResult) {
        final StatisticInfo.PlainProblems problems = parseResult.getPlainProblems();
        final StatisticInfo.PlainProblems filterProblems = parseResult.getFilterProblems();

        List<StatisticInfo.PlainProblems.Problem> listExceptions = problems.getExceptionList();
        List<StatisticInfo.PlainProblems.Problem> listFatals = problems.getFatalList();
        List<StatisticInfo.PlainProblems.Problem> listBuildFingerprints = problems.getBuildFingerprintList();
        List<StatisticInfo.PlainProblems.Problem> listDexOpts = problems.getDexOptList();

        DefaultMutableTreeNode nodeChapter = new DefaultMutableTreeNode(StatisticInfo.ChapterMapping.getChapterTitle(StatisticInfo.ChapterMapping.CHAPTER_PROBLEMS));
        DefaultMutableTreeNode nodeException = new DefaultMutableTreeNode(String.format("Exceptions(%d)", listExceptions.size()));
        DefaultMutableTreeNode nodeFatal = new DefaultMutableTreeNode(String.format("Fatals(%d)", listFatals.size()));
        DefaultMutableTreeNode nodeBuildFingerprint = new DefaultMutableTreeNode(String.format("BuildFingerprints(%d)", listBuildFingerprints.size()));
        DefaultMutableTreeNode nodeDexOpt = new DefaultMutableTreeNode(String.format("DexOpts(%d)", listDexOpts.size()));
        DefaultMutableTreeNode nodeKeyword = new DefaultMutableTreeNode(mFilters);

        nodeChapter.add(nodeKeyword);
        nodeChapter.add(nodeException);
        nodeChapter.add(nodeFatal);
        nodeChapter.add(nodeBuildFingerprint);
        nodeChapter.add(nodeDexOpt);

        addSubTreeNodes2ProblemTreeNode(nodeException, "Exception", listExceptions, parseResult);
        addSubTreeNodes2ProblemTreeNode(nodeFatal, "Fatal", listFatals, parseResult);
        addSubTreeNodes2ProblemTreeNode(nodeBuildFingerprint, "BuildFingerprint", listBuildFingerprints, parseResult);
        addSubTreeNodes2ProblemTreeNode(nodeDexOpt, "DexOpt", listDexOpts, parseResult);

        List<StatisticInfo.PlainProblems.Problem> listFilterExceptions = filterProblems.getExceptionList();
        List<StatisticInfo.PlainProblems.Problem> listFilterFatals = filterProblems.getFatalList();
        List<StatisticInfo.PlainProblems.Problem> listFilterBuildFingerprints = filterProblems.getBuildFingerprintList();
        List<StatisticInfo.PlainProblems.Problem> listFilterDexOpts = filterProblems.getDexOptList();

        nodeException = new DefaultMutableTreeNode(String.format("Exceptions(%d)", listFilterExceptions.size()));
        nodeFatal = new DefaultMutableTreeNode(String.format("Fatals(%d)", listFilterFatals.size()));
        nodeBuildFingerprint = new DefaultMutableTreeNode(String.format("BuildFingerprints(%d)", listFilterBuildFingerprints.size()));
        nodeDexOpt = new DefaultMutableTreeNode(String.format("DexOpts(%d)", listFilterDexOpts.size()));

        nodeKeyword.add(nodeException);
        nodeKeyword.add(nodeFatal);
        nodeKeyword.add(nodeBuildFingerprint);
        nodeKeyword.add(nodeDexOpt);

        addSubTreeNodes2ProblemTreeNode(nodeException, "Exception", listFilterExceptions, parseResult);
        addSubTreeNodes2ProblemTreeNode(nodeFatal, "Fatal", listFilterFatals, parseResult);
        addSubTreeNodes2ProblemTreeNode(nodeBuildFingerprint, "BuildFingerprint", listFilterBuildFingerprints, parseResult);
        addSubTreeNodes2ProblemTreeNode(nodeDexOpt, "DexOpt", listFilterDexOpts, parseResult);

        return nodeChapter;
    }

    private void addSubTreeNodes2ProblemTreeNode(DefaultMutableTreeNode node, String title, List<StatisticInfo.PlainProblems.Problem> problems, BugReportParseEngine.ParseResult parseResult) {
        for (StatisticInfo.PlainProblems.Problem problem : problems) {
            addProblemTreeNode(node, title, problem.getChapterFilterLineAtInfo(), parseResult);
        }
    }

    private void addProblemTreeNode(DefaultMutableTreeNode node, String title, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo chapterFilterLineAtInfo, BugReportParseEngine.ParseResult parseResult) {
        StatisticInfo.CacheLinesStatistic cacheLinesStatistic = parseResult.getCacheLinesStatistic();
        java.util.List<String> listLines = cacheLinesStatistic.getLinesList();

        DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(title);
        node.add(subNode);

        int start = (int) chapterFilterLineAtInfo.getChapterStartLine();
        int end   = (int) chapterFilterLineAtInfo.getChapterEndLine();
        for (int i = start ; i <= end ; i++) {
            DefaultMutableTreeNode subNodeContent = new DefaultMutableTreeNode();
            subNodeContent.setUserObject(getLineTag(i, listLines.get(i), StatisticInfo.ChapterMapping.CHAPTER_PROBLEMS));

            subNode.add(subNodeContent);
        }
    }

    private synchronized void loadContentPartially(JTextPane content, BugReportParseEngine.ParseResult parseResult, int start, int end, int total, int selected, int oldStart, int oldEnd, int scrollToLine) {
        clearTextPane(content);

        StyledDocument styledDocument = content.getStyledDocument();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold(attr, false);
        StyleConstants.setForeground(attr, Color.BLUE);

        List<String> listLines = parseResult.getCacheLinesStatistic().getLinesList();

        int len = end >= total ? total - 1 : end;
        for (int i = start ; i <= len ; i++) {
            String line = listLines.get(i);
            try {
                if (selected >= 0 && i == selected) {
                    StyleConstants.setBackground(attr, Color.PINK);
                    StyleConstants.setBold(attr, true);
                }

                styledDocument.insertString(styledDocument.getLength(), i + "\t", attr);

                int subStart = 0, subEnd = 0, filterLen = mFilters.length();
                do {
                    subStart = StringUtils.kmp_indexOf(line, mFilters, subEnd); //line.indexOf(mFilters, subEnd);
                    if (subStart < 0) {
                        if (subEnd == 0) {
                            styledDocument.insertString(styledDocument.getLength(), line, attr);
                        } else {
                            styledDocument.insertString(styledDocument.getLength(), line.substring(subEnd), attr);
                        }
                    } else {
                        styledDocument.insertString(styledDocument.getLength(), line.substring(subEnd, subStart + subEnd), attr);

                        StyleConstants.setBackground(attr, Color.YELLOW);
                        StyleConstants.setForeground(attr, Color.RED);
                        StyleConstants.setBold(attr, true);

                        styledDocument.insertString(styledDocument.getLength(), mFilters, attr);

                        if (selected >= 0 && i == selected) {
                            StyleConstants.setBackground(attr, Color.PINK);
                        } else {
                            StyleConstants.setBackground(attr, Color.WHITE);
                            StyleConstants.setBold(attr, false);
                        }

                        StyleConstants.setForeground(attr, Color.BLUE);

                        subEnd += subStart + filterLen;
                    }
                } while (subStart >= 0);

                styledDocument.insertString(styledDocument.getLength(), "\n", attr);

                StyleConstants.setBackground(attr, Color.WHITE);
                StyleConstants.setBold(attr, false);
            } catch (BadLocationException blex) {
                Logger.getLogger(this).log(TAG, blex.getMessage(), blex);
                blex.printStackTrace();
            }
        }

        content.updateUI();
    }

    private void switchToTab(JTabbedPane tabbedPane, int tabId) {
//        if (tabbedPane.getSelectedIndex() == tabId)
//            return;

        Integer tabIndex = mTabID2TabIndexMap.get(tabId);
        if (tabIndex == null)
            return;

        tabbedPane.setSelectedIndex(tabIndex);
        tabbedPane.updateUI();
    }

    private static class TreeNodeTag {
        private boolean mHasContent2Extract;
        private boolean mLeafNode;
        private long mOrigStartLine;
        private long mOrigEndLine;
        private Integer mChapterId;
        private String mLine;

        public boolean hasContent2Extract() {
            return mHasContent2Extract;
        }

        public void setHasContent2Extract(boolean hasContent2Extract) {
            mHasContent2Extract = hasContent2Extract;
        }

        public boolean isLeafNode() {
            return mLeafNode;
        }

        public void setLeafNode(boolean leafNode) {
            mLeafNode = leafNode;
        }

        public long getOrigStartLine() {
            return mOrigStartLine;
        }

        public void setOrigStartLine(long origStartLine) {
            mOrigStartLine = origStartLine;
        }

        public long getOrigEndLine() {
            return mOrigEndLine;
        }

        public void setOrigEndLine(long origEndLine) {
            mOrigEndLine = origEndLine;
        }

        public Integer getChapterId() {
            return mChapterId;
        }

        public void setChapterId(Integer chapterId) {
            mChapterId = chapterId;
        }

        public String getLine() {
            return mLine;
        }

        public void setLine(String line) {
            mLine = line;
        }

        public String toString() {
            return mLine;
        }
    }

    private static class ScrollContentWatcher {
        private long mStart;
        private long mEnd;
        private long mIncrement;

        private long mCurStart;
        private long mCurEnd;

        private long mTotal;

        private long mSelectedLine;

        public long getStart() {
            return mStart;
        }

        public void setStart(long start) {
            if (start < 0) {
                mStart = 0;
            } else {
                mStart = start;
            }
        }

        public long getEnd() {
            return mEnd;
        }

        public void setEnd(long end) {
            mEnd = end;
        }

        public long getIncrement() {
            return mIncrement;
        }

        public void setIncrement(long increment) {
            this.mIncrement = increment;
        }

        public long getCurStart() {
            return mCurStart;
        }

        public void setCurStart(long curStart) {
            if (curStart < 0) {
                mCurStart = 0;
            } else {
                mCurStart = curStart;
            }
        }

        public long getCurEnd() {
            return mCurEnd;
        }

        public void setCurEnd(long curEnd) {
            mCurEnd = curEnd;
        }

        public long getSelectedLine() {
            return mSelectedLine;
        }

        public void setSelectedLine(long selectedLine) {
            mSelectedLine = selectedLine;
        }

        public long getTotal() {
            return mTotal;
        }

        public void setTotal(long total) {
            mTotal = total;
        }
    }

    @Override
    public Logger log(String TAG, String level, String msg) {
        Logger logger = Logger.getLogger(this);
        StyledDocument styledDocument = mLogOutput.getStyledDocument();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold(attr, false);
        StyleConstants.setForeground(attr, sLogColorMap.get(level));

        try {
            styledDocument.insertString(styledDocument.getLength(), logger.formatLogString(TAG, level, msg) + "\n", attr);
            mLogOutput.selectAll();
        } catch (BadLocationException blex) {
            Logger.getLogger(this).log(TAG, blex.getMessage(), blex);
        }

        return logger;
    }

    @Override
    public Logger log(String TAG, String msg, Throwable throwable) {
        Logger logger = Logger.getLogger(this);
        StyledDocument styledDocument = mLogOutput.getStyledDocument();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold(attr, false);
        StyleConstants.setForeground(attr, sLogColorMap.get(Logger.LOG_LEVEL_ERROR));

        try {
            styledDocument.insertString(styledDocument.getLength(), logger.formatLogString(TAG, msg, throwable) + "\n", attr);
            mLogOutput.selectAll();
        } catch (BadLocationException blex) {
            Logger.getLogger(this).log(TAG, blex.getMessage(), blex);
        }

        return logger;
    }

    public static class MyTreeCellRenderer extends DefaultTreeCellRenderer {
        private final TreeCellRenderer mRenderer;
        private String mKey;

        public MyTreeCellRenderer(TreeCellRenderer renderer) {
            mRenderer = renderer;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JComponent c = (JComponent) mRenderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
            if (isSelected) {
                c.setOpaque(false);
                c.setForeground(getTextSelectionColor());
            } else {
                c.setOpaque(true);
                if (mKey !=null && !mKey.equals("") && StringUtils.kmp_indexOf(value.toString().toLowerCase(), mKey.toLowerCase()) > -1) {
                    c.setForeground(getTextNonSelectionColor());
                    c.setBackground(Color.YELLOW);
                } else {
                    c.setForeground(getTextNonSelectionColor());
                    c.setBackground(getBackgroundNonSelectionColor());
                }
            }
            return c;
        }

        public void setKey(String key) {
            mKey = key;
        }
    }

    public class MyAdjustmentListener implements AdjustmentListener {
        private ScrollContentWatcher mScrollContentWatcher;
        private BugReportParseEngine.ParseResult mParseResult;
        private JTextPane mContent;

        public MyAdjustmentListener(ScrollContentWatcher scrollContentWatcher, BugReportParseEngine.ParseResult parseResult, JTextPane content) {
            mScrollContentWatcher = scrollContentWatcher;
            mParseResult = parseResult;
            mContent = content;
        }

        @Override
        public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
            if (mParseResult == null || !mUserDragContentScroll)
                return;

            Adjustable adjustable = e.getAdjustable();
//                System.out.println("Adjustable.BlockIncrement = " + adjustable.getBlockIncrement());
//                System.out.println("Adjustable.UnitIncrement = " + adjustable.getUnitIncrement());
//                System.out.println("Adjustable.Minimum = " + adjustable.getMinimum());
//                System.out.println("Adjustable.Maximum = " + adjustable.getMaximum());
//                System.out.println("Adjustable.Orientation = " + adjustable.getOrientation());
//                System.out.println("Adjustable.Value = " + adjustable.getValue());
//                System.out.println("Adjustable.VisibleAmount = " + adjustable.getVisibleAmount());

            int value = adjustable.getValue();
            //int visibleAmount = adjustable.getVisibleAmount();
            int curStartLine  = (int) mScrollContentWatcher.getStart();
            int curEndLine = (int) mScrollContentWatcher.getEnd();
            int oldStartLine = curStartLine;
            int oldEndLine = curEndLine;
            int increment  = (int) mScrollContentWatcher.getIncrement();
            int maxLines = (int) mParseResult.getCacheLinesStatistic().getTotalLine();

            if (value == 0 && curStartLine > 0) {
                curStartLine -= increment;

                if (curStartLine < 0)
                    curStartLine = 0;

                mScrollContentWatcher.setStart(curStartLine);

                loadContentPartially(mContent, mParseResult, curStartLine, curEndLine, maxLines, (int) mScrollContentWatcher.getSelectedLine(), oldStartLine, oldEndLine, oldStartLine);
            } else if (value + adjustable.getVisibleAmount() == adjustable.getMaximum() && curEndLine < maxLines) {
                curEndLine += increment;

                if (curEndLine > maxLines)
                    curEndLine = maxLines;

                mScrollContentWatcher.setEnd(curEndLine);

                loadContentPartially(mContent, mParseResult, curStartLine, curEndLine, maxLines, (int) mScrollContentWatcher.getSelectedLine(), oldStartLine, oldEndLine, oldEndLine);
            }
        }

        public synchronized void setParseResult(BugReportParseEngine.ParseResult parseResult) {
            mParseResult = parseResult;
        }
    };
}

