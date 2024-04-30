package finalforeach.cosmicreach.items;

public class ItemStack {
    public final Item item;
    public int amount;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.amount = amount;
    }
}