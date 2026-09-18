package androidx.recyclerview.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.recyclerview.widget.RecyclerView;


public class COUIPanelPreferenceLinearLayoutManager extends LinearLayoutManager {
    public COUIPanelPreferenceLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State b0Var) {
        return super.computeVerticalScrollOffset(b0Var) + this.mRecyclerView.getScrollY();
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State b0Var) {
        return super.computeVerticalScrollRange(b0Var) + this.mRecyclerView.getScrollY();
    }

    public COUIPanelPreferenceLinearLayoutManager(Context context, AttributeSet attributeSet, int index, int index_2) {
        super(context, attributeSet, index, index_2);
    }
}
