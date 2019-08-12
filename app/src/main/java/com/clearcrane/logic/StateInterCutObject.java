package com.clearcrane.logic;

import android.util.Log;

public class StateInterCutObject extends PrisonOrganism {

    private final String TAG = "StateInterCutObject";
    private StateInterCutOrgan mStateInterCutOrgam;

    public StateInterCutObject(StateInterCutOrgan organ) {
        this.mStateInterCutOrgam = organ;
        this.workOrgan = mStateInterCutOrgam;
    }

    @Override
    public void setWorking(boolean isWorking) {
        // TODO,FIXME
        super.setWorking(isWorking);
        if (workOrgan != null) {
            workOrgan.setWorking(isWorking);
        }

    }

    @Override
    public boolean isAwake() {
        Log.e(TAG, "isAwake : isAlive " + isAlive());
        return isAlive() && mStateInterCutOrgam.isAlive();
    }

}
