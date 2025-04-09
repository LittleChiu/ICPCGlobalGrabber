package me.yeoc.grabber.object;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

public class ContestTree {
    private JTree tree;
    private DefaultTreeModel model;

    public ContestTree(JTree tree) {
        this.tree = tree;
        this.model = (DefaultTreeModel) tree.getModel();
    }
    public ContestTree(JTree tree, List<Contest> roots) {
        this.tree = tree;

        // 构建虚拟根节点
        Contest virtualContest = new Contest();
        virtualContest.setLabel("全部比赛"); // 虚拟根
        virtualContest.setLeaf(false);

        ContestNode virtualRoot = new ContestNode(virtualContest);

        // 添加多个根节点
        for (Contest contest : roots) {
            ContestNode node = new ContestNode(contest);
            node.add(new ReloadNode());
            virtualRoot.add(node);
        }

        model = new DefaultTreeModel(virtualRoot);
        tree.setModel(model);

        tree.expandPath(new TreePath(virtualRoot.getPath()));

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                if (value instanceof ContestNode) {
                    ContestNode node = (ContestNode) value;
                    Contest contest = node.getContest();

                    Icon icon;
                    if (contest.isLeaf()) {
                        if (contest.isActive()){
                            icon = new FlatSVGIcon("icons/pair.svg", 16, 16);
                        }else{
                            icon = new FlatSVGIcon("icons/inavailable.svg", 16, 16);
                        }
                        label.setIcon(icon);
                    } else {
                        label.setIcon(new FlatSVGIcon("icons/tree.svg", 16, 16));
                    }
                }

                return label;
            }
        });
    }
    public ContestNode addNode(ContestNode parent, Contest data) {
        ContestNode newNode = new ContestNode(data);
        model.insertNodeInto(newNode, parent, parent.getChildCount());
        tree.scrollPathToVisible(new TreePath(newNode.getPath()));
        return newNode;
    }

    public void deleteNode(ContestNode node) {
        if (node.getParent() != null) {
            model.removeNodeFromParent(node);
        }
    }

    public void updateNode(ContestNode node, Contest newData) {
        node.setUserObject(newData);
        model.nodeChanged(node);
    }

    public void setChildren(ContestNode parent, List<Contest> children) {
        if (!parent.isLeafNode()) {
            parent.setChildren(children);
            model.reload(parent); // 通知 UI 刷新
        }
    }
}

