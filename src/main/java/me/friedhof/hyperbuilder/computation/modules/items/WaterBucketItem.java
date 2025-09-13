package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

public class WaterBucketItem extends BaseItem {

    public WaterBucketItem(int count) {
        super(Material.BUCKET_WATER, "Water Bucket", 1, count);
    }
    public WaterBucketItem() {
        super(Material.BUCKET_WATER, "Water Bucket", 1, 1);
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new WaterBucketItem(newCount);
    }


}
