package com.coui.component.responsiveui;

import android.content.Context;
import com.coui.component.responsiveui.layoutgrid.ILayoutGrid;
import com.coui.component.responsiveui.status.IWindowStatus;
import com.coui.component.responsiveui.window.LayoutGridWindowSize;

public interface IResponsiveUI extends ILayoutGrid, IWindowStatus {
    void onConfigurationChanged(Context context, LayoutGridWindowSize windowSize);

    void rebuild(Context context, LayoutGridWindowSize windowSize);

    String showLayoutGridInfo();

    String showWindowStatusInfo();
}
