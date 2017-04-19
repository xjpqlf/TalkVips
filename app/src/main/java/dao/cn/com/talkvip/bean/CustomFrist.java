package dao.cn.com.talkvip.bean;

/**
 * @name dao.cn.com.talkvip.bean
 * @class name：TalkVip
 * @class describe
 * @anthor uway QQ:343251588
 * @time 2017/4/18 14:57
 * @change uway
 * @chang 2017/4/18 14:57
 * @class describe
 */

public class CustomFrist {
    private Custom mCustom;
    private boolean isFirst;


    public CustomFrist() {
    }

    public CustomFrist(Custom custom, boolean isFirst) {
        mCustom = custom;
        this.isFirst = isFirst;
    }

    public Custom getCustom() {
        return mCustom;
    }

    public void setCustom(Custom custom) {
        mCustom = custom;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    @Override
    public String toString() {
        return "CustomFrist{" +
                "mCustom=" + mCustom +
                ", isFirst=" + isFirst +
                '}';
    }
}
