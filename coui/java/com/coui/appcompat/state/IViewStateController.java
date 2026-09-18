package com.coui.appcompat.state;


public interface IViewStateController {
    void onViewStateChanged(int state);

    void release();
}
