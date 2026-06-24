package com.example.cloudtectonics.math;

import java.util.List;

/**
 * 建筑屋顶几何生成策略接口。
 */
public interface IRoofGenerator {
    
    /**
     * 生成屋顶结构组件（瓦片、椽条、檩条、望板等）
     * 
     * @param config 营造系统当前参数配置
     * @param showRoof 是否显示瓦面
     * @param showRafters 是否显示椽木
     * @param showPurlins 是否显示檩条
     * @return 生成的构件列表
     */
    List<StructureComponent> generate(ParametricBuildingGenerator.BuildingConfig config, 
                                      boolean showRoof, boolean showRafters, boolean showPurlins);
}
