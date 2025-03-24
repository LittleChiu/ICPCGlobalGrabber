package me.yeoc.grabber.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.SneakyThrows;
import me.yeoc.grabber.object.*;
import me.yeoc.grabber.service.ICPCGlobalService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MainGUI {
    private JPanel mainPanel;
    private JTextField auth;
    private JPanel configuration;
    private JTree contests;
    private JTextField institutionName;
    private JButton queryInstitution;
    private JTable institutionList;
    private JTextField teamName;
    private JTextField contestId;
    private JTextField createTeamInstitution;
    private JTextArea log;
    private JButton createTeamButton;
    private JButton refreshContestButton;
    private JButton informRefreshButton;
    private JTextField season;

    private ContestTree contestTree;

    @SneakyThrows
    public static void start() {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        JFrame frame = new JFrame("ICPC Global Grabber Ver 1.0 by LittleQiu233");
        frame.setContentPane(new MainGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    ICPCGlobalService service;

    public MainGUI() {
        service = new ICPCGlobalService();
        refreshContestButton.addActionListener(e -> {
            refreshContestButton.setEnabled(false); // 防止重复点击
            info("正在加载全局数据...");
            SwingWorker<List<Contest>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Contest> doInBackground() throws Exception {
                    int seasonId = Integer.parseInt(season.getText());
                    return service.getGlobal(seasonId);
                }

                @Override
                protected void done() {
                    try {
                        List<Contest> globalList = get();
                        contestTree = new ContestTree(contests, globalList);
                        info("全局数据加载完成！");
                    } catch (Exception ex) {
                        info("加载失败：" + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        refreshContestButton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });
        informRefreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                info("更新成功！" + auth.getText());
                service = new ICPCGlobalService(auth.getText());
            }
        });


        contests.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
//                System.out.println(e.getPath().toString());
                if (auth.getText().isEmpty()) {
                    info("请添加Auth");
                    return;
                }
                TreePath path = e.getPath();
                Object lastComponent = path.getLastPathComponent();
                if (lastComponent instanceof ReloadNode component) {
                    TreeNode parent = component.getParent();
                    if (parent instanceof ContestNode contestNode) {
                        info("开始刷新节点！" + contestNode.getContest().getLabel());

                        SwingWorker<List<Contest>, Void> worker = new SwingWorker<>() {
                            @Override
                            protected List<Contest> doInBackground() throws Exception {
                                return service.getContest(contestNode.getContest().getId());
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<Contest> contestList = get();

                                    contestNode.removeAllChildren();
                                    if (contestList.isEmpty()) {
                                        contestNode.add(new TextNode("空!"));
                                    } else {
                                        contestNode.setChildrenWithReloadNode(contestList);
                                    }
                                    contestNode.add(new ReloadNode());
                                    DefaultTreeModel model = (DefaultTreeModel) contests.getModel();
                                    model.reload(contestNode);
                                    info("刷新完成！");
                                } catch (Exception e) {
                                    info("刷新失败：" + e.getMessage());
                                }
                            }
                        };

                        worker.execute();
//                        info("开始刷新节点！" + contestNode.getContest().getLabel());
//                        List<Contest> contest = service.getContest(contestNode.getContest().getId());
//                        if (contest.isEmpty()) {
//                            contestNode.removeAllChildren();
//                            contestNode.add(new TextNode("空!"));
//
//                        } else {
//                            contestNode.setChildrenWithReloadNode(contest);
//
//                        }
//
//                        contestNode.add(new ReloadNode());
//                        DefaultTreeModel model = (DefaultTreeModel) contests.getModel();
//                        model.reload(contestNode);
                    }

                } else if (lastComponent instanceof ContestNode selectedNode) {

                    if (selectedNode.isLeafNode()) {
                        Contest contest = selectedNode.getContest();
                        info(contest.getId() + " " + contest.getLabel() + " active: " + contest.isActive());
                        if (contest.isActive()) {
                            info("Warning! 这个比赛还没有开启,请慎重创队!");
                        }
                        contestId.setText(String.valueOf(contest.getId()));

                    }
                }
//                else if (lastComponent instanceof ContestNode) {
//                    ContestNode selectedNode = (ContestNode) lastComponent;
//                    Contest contest = selectedNode.getContest();
//
//                    System.out.println("点击了: " + contest.getLabel());
//
//                    // 如果是非叶子节点并且还没有加载过子节点，就加载
//                    if (!contest.isLeaf() && selectedNode.getChildCount() == 0) {
//                        // 模拟获取子节点（你可以换成实际数据源，比如数据库、接口等）
//                        // 设置子节点
//                        System.out.println("正在加载ing");
//                        selectedNode.setChildren(service.getContest(contest.getId()));
//                        selectedNode.add(new ReloadNode());
//                        // 通知树刷新节点
//                        DefaultTreeModel model = (DefaultTreeModel) contests.getModel();
//                        model.reload(selectedNode);
//                    }
//                }
            }
        });

        queryInstitution.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO AUTH判断
                info("正在刷新机构...");
                queryInstitution.setEnabled(false);
                new SwingWorker<List<Institution>, Void>() {
                    @Override
                    protected List<Institution> doInBackground() throws Exception {
                        return service.getInstitution(institutionName.getText());
                    }

                    @Override
                    @SneakyThrows
                    protected void done() {
                        try {
                            String[] columnNames = {"编号", "名称", "国家"};
                            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
                            for (Institution ins : get()) {
                                model.addRow(new Object[]{ins.getId(), ins.getName(), ins.getCountry()});
                            }
                            institutionList.setModel(model);
                            info("刷新完毕！");
                        } catch (Exception e) {
                            info("刷新失败：" + e.getMessage());
                        } finally {
                            queryInstitution.setEnabled(true);
                        }


                    }
                }.execute();
//                List<Institution> institution = ;


            }
        });
        createTeamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                info("正在请求创建队伍...");
                createTeamButton.setEnabled(false);  // 防止重复点击

                SwingWorker<String, Void> worker = new SwingWorker<>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        // 在后台执行耗时操作
                        return service.createTeam(
                                teamName.getText(),
                                Integer.parseInt(contestId.getText()),
                                Integer.parseInt(createTeamInstitution.getText())
                        );
                    }

                    @Override
                    protected void done() {
                        try {
                            String result = get();
                            info(result);
                        } catch (Exception e) {
                            info("创建失败：" + e.getMessage());
                        } finally {
                            createTeamButton.setEnabled(true);
                        }
                    }
                };

                worker.execute();
            }
        });
        institutionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = institutionList.rowAtPoint(e.getPoint());
                int column = institutionList.columnAtPoint(e.getPoint());
                createTeamInstitution.setText(String.valueOf(institutionList.getValueAt(row, 0)));
            }
        });
    }

    public void info(String str) {
        log.append(str + "\n");
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

//        contests.add(new DefaultMutableTreeNode("CNM"));
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        configuration = new JPanel();
        configuration.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(configuration, gbc);
        configuration.setBorder(BorderFactory.createTitledBorder(null, "配置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        configuration.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        auth = new JTextField();
        auth.setText("");
        panel1.add(auth, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Auth");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        configuration.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        informRefreshButton = new JButton();
        informRefreshButton.setText("更新");
        panel2.add(informRefreshButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(78, 37), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(14, 37), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(panel3, gbc);
        panel3.setBorder(BorderFactory.createTitledBorder(null, "比赛选择", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, -1), null, 0, false));
        contests = new JTree();
        scrollPane1.setViewportView(contests);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        refreshContestButton = new JButton();
        refreshContestButton.setText("查询");
        panel4.add(refreshContestButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("赛季Id");
        panel4.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        season = new JTextField();
        season.setText("2026");
        panel4.add(season, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(panel5, gbc);
        panel5.setBorder(BorderFactory.createTitledBorder(null, "机构建议", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        institutionName = new JTextField();
        institutionName.setText("");
        panel6.add(institutionName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("机构名");
        panel6.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        queryInstitution = new JButton();
        queryInstitution.setText("查询");
        panel6.add(queryInstitution, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(-1, 200), 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel7.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(-1, 100), null, 0, false));
        institutionList = new JTable();
        scrollPane2.setViewportView(institutionList);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(panel8, gbc);
        panel8.setBorder(BorderFactory.createTitledBorder(null, "创建队伍", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel9, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        teamName = new JTextField();
        teamName.setText("");
        panel9.add(teamName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("队伍名称");
        panel9.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel8.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel10, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("比赛ID");
        panel10.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contestId = new JTextField();
        panel10.add(contestId, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel11, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("机构ID");
        panel11.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createTeamInstitution = new JTextField();
        panel11.add(createTeamInstitution, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        createTeamButton = new JButton();
        createTeamButton.setText("启动！");
        panel8.add(createTeamButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel8.add(spacer3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(panel12, gbc);
        panel12.setBorder(BorderFactory.createTitledBorder(null, "Log", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel12.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        log = new JTextArea();
        scrollPane3.setViewportView(log);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
