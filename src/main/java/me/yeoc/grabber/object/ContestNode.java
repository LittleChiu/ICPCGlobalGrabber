package me.yeoc.grabber.object;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

public class ContestNode extends DefaultMutableTreeNode {
    public ContestNode(Contest contest) {
        super(contest);
    }
    public Contest getContest() {
        return (Contest) getUserObject();
    }

    public boolean isLeafNode() {
        return getContest().isLeaf(); // 根据 Contest 对象里的 leaf 字段判断
    }

    @Override
    public boolean isLeaf() {
        return isLeafNode(); // 让 JTree 判断时也用这个逻辑
    }

    public void setChildren(List<Contest> childContests) {
        if (isLeafNode()) return; // 是叶子节点就不添加子节点
        removeAllChildren(); // 先清空旧的子节点
        for (Contest child : childContests) {
            add(new ContestNode(child));
        }
    }
    public void setChildrenWithReloadNode(List<Contest> childContests) {
        if (isLeafNode()) return; // 是叶子节点就不添加子节点
        removeAllChildren(); // 先清空旧的子节点
        for (Contest child : childContests) {
            ContestNode contestNode = new ContestNode(child);
            contestNode.add(new ReloadNode());
            add(contestNode);
        }
    }
    @Override
    public String toString() {
        return getContest().getLabel(); // 控制在 JTree 上显示的名称
    }
}
