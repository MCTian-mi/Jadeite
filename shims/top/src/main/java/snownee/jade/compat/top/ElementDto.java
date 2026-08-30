package snownee.jade.compat.top;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * Data transfer object for a single TheOneProbe element, serialized through
 * Jade's server-data NBT channel.
 * <p>
 * 1.12.2 backport: unchanged from the Jade-embedded original (no Jade imports).
 */
public class ElementDto {

	public static final int TEXT = 0;
	public static final int ITEM = 1;
	public static final int PROGRESS = 2;
	public static final int ICON = 3;
	public static final int HORIZONTAL = 4;
	public static final int VERTICAL = 5;
	/** A third-party custom element (anything passed to {@code IProbeInfo.element(IElement)}). */
	public static final int CUSTOM = 6;

	public int type;
	public String text = "";
	public ItemStack stack = ItemStack.EMPTY;
	public long current;
	public long max;
	public List<ElementDto> children = new ArrayList<>();
	/** Factory id for {@link #CUSTOM} elements (see {@code ITheOneProbe.registerElementFactory}). */
	public int factoryId;
	/** Element payload for {@link #CUSTOM} elements: the bytes written by {@code IElement.toBytes(ByteBuf)}. */
	public byte[] data = new byte[0];
	/** Progress-bar style (see {@link #PROGRESS}); -1 = default/unspecified. */
	public int progressFilledColor = -1;
	public int progressAlternateColor = -1;
	public int progressBorderColor = -1;
	public int progressBackgroundColor = -1;
	public boolean progressShowText = true;
	public int progressNumberFormat;
	public String progressPrefix = "";
	public String progressSuffix = "";
	/**
	 * TOP provider ID that produced this element (or the block-accessor sentinel). Populated on the
	 * server so the client can filter elements per-provider; empty for elements captured before the
	 * provider tagging was introduced.
	 */
	public String providerId = "";

	public NBTTagCompound toNbt() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("type", type);
		if (!text.isEmpty()) tag.setString("text", text);
		if (!stack.isEmpty()) {
			tag.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
			tag.setInteger("stackCount", stack.getCount());
		}
		if (type == PROGRESS) {
			tag.setLong("current", current);
			tag.setLong("max", max);
			if (progressFilledColor != -1) tag.setInteger("pFilled", progressFilledColor);
			if (progressAlternateColor != -1) tag.setInteger("pAlt", progressAlternateColor);
			if (progressBorderColor != -1) tag.setInteger("pBorder", progressBorderColor);
			if (progressBackgroundColor != -1) tag.setInteger("pBg", progressBackgroundColor);
			if (!progressShowText) tag.setBoolean("pText", false);
			if (progressNumberFormat != 0) tag.setInteger("pNum", progressNumberFormat);
			if (!progressPrefix.isEmpty()) tag.setString("pPrefix", progressPrefix);
			if (!progressSuffix.isEmpty()) tag.setString("pSuffix", progressSuffix);
		}
		if (type == CUSTOM) {
			tag.setInteger("factoryId", factoryId);
			tag.setByteArray("data", data);
		}
		if (!children.isEmpty()) {
			NBTTagList list = new NBTTagList();
			for (ElementDto child : children) {
				list.appendTag(child.toNbt());
			}
			tag.setTag("children", list);
		}
		if (!providerId.isEmpty()) {
			tag.setString("providerId", providerId);
		}
		return tag;
	}

	public static ElementDto fromNbt(NBTTagCompound tag) {
		ElementDto dto = new ElementDto();
		dto.type = tag.getInteger("type");
		dto.text = tag.getString("text");
		if (tag.hasKey("stack", Constants.NBT.TAG_COMPOUND)) {
			dto.stack = new ItemStack(tag.getCompoundTag("stack"));
			if (tag.hasKey("stackCount")) {
				dto.stack.setCount(tag.getInteger("stackCount"));
			}
		}
		dto.current = tag.getLong("current");
		dto.max = tag.getLong("max");
		if (dto.type == PROGRESS) {
			if (tag.hasKey("pFilled")) dto.progressFilledColor = tag.getInteger("pFilled");
			if (tag.hasKey("pAlt")) dto.progressAlternateColor = tag.getInteger("pAlt");
			if (tag.hasKey("pBorder")) dto.progressBorderColor = tag.getInteger("pBorder");
			if (tag.hasKey("pBg")) dto.progressBackgroundColor = tag.getInteger("pBg");
			if (tag.hasKey("pText")) dto.progressShowText = tag.getBoolean("pText");
			if (tag.hasKey("pNum")) dto.progressNumberFormat = tag.getInteger("pNum");
			dto.progressPrefix = tag.getString("pPrefix");
			dto.progressSuffix = tag.getString("pSuffix");
		}
		if (dto.type == CUSTOM) {
			dto.factoryId = tag.getInteger("factoryId");
			dto.data = tag.getByteArray("data");
		}
		if (tag.hasKey("children", Constants.NBT.TAG_LIST)) {
			NBTTagList list = tag.getTagList("children", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				dto.children.add(fromNbt(list.getCompoundTagAt(i)));
			}
		}
		dto.providerId = tag.getString("providerId");
		return dto;
	}

	/**
	 * Recursively stamps this element and all its children with a TOP provider ID, so the client
	 * can filter a whole element tree (including nested layouts) per-provider.
	 */
	public void stampProvider(String id) {
		providerId = id;
		for (ElementDto child : children) {
			child.stampProvider(id);
		}
	}
}
