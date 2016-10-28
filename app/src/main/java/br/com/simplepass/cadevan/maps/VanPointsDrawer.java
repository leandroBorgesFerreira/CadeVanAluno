package br.com.simplepass.cadevan.maps;

import java.util.Map;

import br.com.simplepass.cadevan.domain.Van;

/**
 * Created by leandro on 11/18/15.
 */
public interface VanPointsDrawer {
    boolean isShowingNoInternetMessage();
    void drawVanPoints(Map<Integer, Van> vanList);
    void drawFixedMapPoints();
    void showNoInternetMessage(boolean show);
    void drawSingleVan(Van van);
}
