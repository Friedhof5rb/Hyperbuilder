package me.friedhof.hyperbuilder.computation.modules.items;

import me.friedhof.hyperbuilder.computation.modules.Material;

public class BucketItem extends BaseItem {

    public BucketItem(int count) {
        super(Material.BUCKET, "Bucket", 1, count);
    }
    public BucketItem() {
        super(Material.BUCKET, "Bucket", 1, 1);
    }
    @Override
    public BaseItem withCount(int newCount) {
        return new BucketItem(newCount);
    }
}
