package com.clearcrane.interfaces;

public abstract interface AudioVolumeChangeListener
{
  public abstract void updateVolume(int currentVolume, int maxVolume);
}
