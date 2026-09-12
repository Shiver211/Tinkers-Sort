package com.shiver.tinkers_sort.client.gui;

import com.shiver.tinkers_sort.book.MaterialSectionManager;
import com.shiver.tinkers_sort.config.ModConfig;
import com.shiver.tinkers_sort.sorting.SortMode;
import com.shiver.tinkers_sort.sorting.SortOrder;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.tconstruct.library.materials.MaterialTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSortToolbar {

    public static final int TOOLBAR_WIDTH = 340;
    public static final int TOOLBAR_HEIGHT = 20;
    public static final int TAB_HEIGHT = 14;

    private static final String[] BOW_CATEGORIES = new String[] { "all", "bow", "bowstring", "shaft", "fletching" };

    private boolean visible = false;
    private String currentSection = "materials";
    private String activeBowCategory = "all";

    private int x;
    private int y;

    private GuiTextField searchField;
    private boolean isDropdownOpen = false;

    // Relative button offsets
    private final int searchX = 4;
    private final int searchY = 2;
    private final int searchW = 100;
    private final int searchH = 16;

    private final int clearX = 106;
    private final int clearY = 2;
    private final int clearW = 14;
    private final int clearH = 16;

    private final int modeX = 124;
    private final int modeY = 2;
    private final int modeW = 126;
    private final int modeH = 16;

    private final int orderX = 254;
    private final int orderY = 2;
    private final int orderW = 54;
    private final int orderH = 16;

    private final int resetX = 312;
    private final int resetY = 2;
    private final int resetW = 22;
    private final int resetH = 16;

    private static class TabRect {
        String type;
        int x, y, w, h;
    }

    public boolean isBowMaterials() {
        return "bowmaterials".equalsIgnoreCase(currentSection) || MaterialSectionManager.isBowCategory(currentSection);
    }

    public String getActiveTarget() {
        if (isBowMaterials()) {
            if ("all".equalsIgnoreCase(activeBowCategory) || "bowmaterials".equalsIgnoreCase(activeBowCategory)) {
                return MaterialTypes.BOW;
            }
            return activeBowCategory;
        }
        return currentSection;
    }

    private List<TabRect> getTabRects(FontRenderer fr) {
        List<TabRect> rects = new ArrayList<>();
        int curX = x + 4;
        int tabY = y - TAB_HEIGHT;
        for (String type : BOW_CATEGORIES) {
            String name = I18n.format("tinkers_sort.category." + type);
            int tabW = fr.getStringWidth(name) + 8;
            TabRect tr = new TabRect();
            tr.type = type;
            tr.x = curX;
            tr.y = tabY;
            tr.w = tabW;
            tr.h = TAB_HEIGHT;
            rects.add(tr);
            curX += tabW + 2;
        }
        return rects;
    }

    public void init(GuiBook guiBook) {
        this.x = guiBook.width / 2 - TOOLBAR_WIDTH / 2;
        this.y = guiBook.height / 2 - GuiBook.PAGE_HEIGHT_UNSCALED / 2 + ModConfig.toolbarYOffset;
        if (this.y < 16) {
            this.y = 16;
        }

        SectionData sec = BookGuiHandler.getCurrentSection(guiBook);
        if (sec != null && sec.name != null) {
            this.currentSection = sec.name.toLowerCase();
        } else {
            this.currentSection = "materials";
        }

        String detected = MaterialSectionManager.detectCurrentBowCategory(guiBook);
        this.activeBowCategory = detected != null ? detected : "all";

        FontRenderer fr = guiBook.mc.fontRenderer;
        this.searchField = new GuiTextField(1001, fr, x + searchX + 2, y + searchY + 2, searchW - 4, searchH - 4);
        this.searchField.setMaxStringLength(40);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setText(MaterialSectionManager.getCurrentQuery(this.currentSection));
        this.isDropdownOpen = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            this.isDropdownOpen = false;
            if (this.searchField != null) {
                this.searchField.setFocused(false);
            }
        }
    }

    public void setCurrentSection(String sectionName) {
        if (sectionName == null) sectionName = "materials";
        String normalized = sectionName.toLowerCase();
        if (!normalized.equals(this.currentSection)) {
            this.currentSection = normalized;
            this.isDropdownOpen = false;
            if (this.searchField != null) {
                this.searchField.setText(MaterialSectionManager.getCurrentQuery(this.currentSection));
                this.searchField.setFocused(false);
            }
        }
    }

    public void draw(GuiBook guiBook, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        FontRenderer fr = guiBook.mc.fontRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Detect active bow category from book pages
        if (isBowMaterials()) {
            String detected = MaterialSectionManager.detectCurrentBowCategory(guiBook);
            if (detected != null) {
                this.activeBowCategory = detected;
            }
        }

        String target = getActiveTarget();

        // 0. Draw Category Tabs if bow materials
        TabRect hoveredTab = null;
        if (isBowMaterials()) {
            List<TabRect> tabs = getTabRects(fr);
            for (TabRect tr : tabs) {
                boolean isHover = isHovered(mouseX, mouseY, tr.x, tr.y, tr.w, tr.h);
                boolean isActive = tr.type.equalsIgnoreCase(activeBowCategory);
                if (isHover) hoveredTab = tr;

                String name = I18n.format("tinkers_sort.category." + tr.type);
                int textW = fr.getStringWidth(name);

                if (isActive) {
                    drawBorderedRect(tr.x, tr.y, tr.w, tr.h + 1, 0xFF9E8A74, 0xEE2A2A2A);
                    fr.drawString(name, tr.x + (tr.w - textW) / 2, tr.y + 3, 0xFFFFAA00);
                } else if (isHover) {
                    drawBorderedRect(tr.x, tr.y, tr.w, tr.h, 0xFF7A7064, 0xDD2A2A2A);
                    fr.drawString(name, tr.x + (tr.w - textW) / 2, tr.y + 3, 0xFFFFFFFF);
                } else {
                    drawBorderedRect(tr.x, tr.y, tr.w, tr.h, 0xFF4A443C, 0xBB181818);
                    fr.drawString(name, tr.x + (tr.w - textW) / 2, tr.y + 3, 0xFFAAAAAA);
                }
            }
        }

        // 1. Draw Toolbar Outer Box
        drawBorderedRect(x, y, TOOLBAR_WIDTH, TOOLBAR_HEIGHT, 0xFF5A5248, 0xEE1E1E1E);

        // 2. Draw Search Box
        if (ModConfig.enableSearch) {
            int sfBorder = (searchField != null && searchField.isFocused()) ? 0xFF9E8A74 : 0xFF3E3A34;
            drawBorderedRect(x + searchX, y + searchY, searchW, searchH, sfBorder, 0xDD111111);

            if (searchField != null) {
                if (searchField.getText().isEmpty() && !searchField.isFocused()) {
                    fr.drawString(I18n.format("tinkers_sort.gui.search"), x + searchX + 4, y + searchY + 4, 0x777777);
                } else {
                    searchField.drawTextBox();
                }
            }

            // Clear Button [✕]
            boolean hasQuery = searchField != null && !searchField.getText().isEmpty();
            boolean hoverClear = isHovered(mouseX, mouseY, x + clearX, y + clearY, clearW, clearH);
            int clearBorder = hoverClear ? 0xFF8A7E6C : 0xFF4A443C;
            int clearBg = hoverClear ? 0xDD3E3E3E : 0xBB2A2A2A;
            drawBorderedRect(x + clearX, y + clearY, clearW, clearH, clearBorder, clearBg);
            String clearSymbol = "✕";
            int clearColor = hasQuery ? (hoverClear ? 0xFFFF6666 : 0xFFDDDDDD) : 0xFF666666;
            fr.drawString(clearSymbol, x + clearX + (clearW - fr.getStringWidth(clearSymbol)) / 2 + 1, y + clearY + 4, clearColor);
        }

        // 3. Draw Sort Mode Button
        SortMode currentMode = MaterialSectionManager.getCurrentMode(target);
        boolean hoverMode = isHovered(mouseX, mouseY, x + modeX, y + modeY, modeW, modeH);
        int modeBorder = (hoverMode || isDropdownOpen) ? 0xFF9E8A74 : 0xFF4A443C;
        int modeBg = (hoverMode || isDropdownOpen) ? 0xDD3E3E3E : 0xBB2A2A2A;
        drawBorderedRect(x + modeX, y + modeY, modeW, modeH, modeBorder, modeBg);

        String modeText = currentMode.getDisplayName() + " ▾";
        int modeTextW = fr.getStringWidth(modeText);
        fr.drawString(modeText, x + modeX + (modeW - modeTextW) / 2, y + modeY + 4, 0xFFE0E0E0);

        // 4. Draw Sort Order Button
        SortOrder currentOrder = MaterialSectionManager.getCurrentOrder(target);
        boolean hoverOrder = isHovered(mouseX, mouseY, x + orderX, y + orderY, orderW, orderH);
        int orderBorder = hoverOrder ? 0xFF9E8A74 : 0xFF4A443C;
        int orderBg = hoverOrder ? 0xDD3E3E3E : 0xBB2A2A2A;
        drawBorderedRect(x + orderX, y + orderY, orderW, orderH, orderBorder, orderBg);

        String orderText = currentOrder.getSymbol() + " " + currentOrder.getDisplayName();
        int orderTextW = fr.getStringWidth(orderText);
        fr.drawString(orderText, x + orderX + (orderW - orderTextW) / 2, y + orderY + 4, 0xFFE0E0E0);

        // 5. Draw Reset Button
        boolean hoverReset = isHovered(mouseX, mouseY, x + resetX, y + resetY, resetW, resetH);
        int resetBorder = hoverReset ? 0xFF9E8A74 : 0xFF4A443C;
        int resetBg = hoverReset ? 0xDD3E3E3E : 0xBB2A2A2A;
        drawBorderedRect(x + resetX, y + resetY, resetW, resetH, resetBorder, resetBg);

        String resetText = "↺";
        int resetTextW = fr.getStringWidth(resetText);
        fr.drawString(resetText, x + resetX + (resetW - resetTextW) / 2 + 1, y + resetY + 4, hoverReset ? 0xFFFFAA00 : 0xFFCCCCCC);

        // 6. Draw Dropdown if open
        if (isDropdownOpen) {
            List<SortMode> modes = SortMode.getApplicableModes(target);
            int dropX = x + modeX;
            int dropY = y + modeY + modeH + 2;
            int itemH = 15;
            int dropH = modes.size() * itemH + 4;
            int dropW = modeW + 10;

            // Render on top
            GlStateManager.translate(0, 0, 300);
            drawBorderedRect(dropX, dropY, dropW, dropH, 0xFF8A7E6C, 0xF0181818);

            for (int i = 0; i < modes.size(); i++) {
                SortMode m = modes.get(i);
                int itemY = dropY + 2 + i * itemH;
                boolean hoverItem = isHovered(mouseX, mouseY, dropX + 1, itemY, dropW - 2, itemH);

                if (hoverItem) {
                    Gui.drawRect(dropX + 2, itemY, dropX + dropW - 2, itemY + itemH, 0x44FFFFFF);
                }

                boolean isSelected = m == currentMode;
                String itemText = (isSelected ? "§6✓ " : "  ") + m.getDisplayName();
                fr.drawString(itemText, dropX + 4, itemY + 3, isSelected ? 0xFFFFAA00 : 0xFFE0E0E0);
            }
            GlStateManager.translate(0, 0, -300);
        }

        // 7. Render Tooltips
        if (!isDropdownOpen) {
            boolean hoverClear = ModConfig.enableSearch && isHovered(mouseX, mouseY, x + clearX, y + clearY, clearW, clearH);
            boolean hoverSearch = ModConfig.enableSearch && isHovered(mouseX, mouseY, x + searchX, y + searchY, searchW, searchH);
            if (hoveredTab != null) {
                GuiUtils.drawHoveringText(Collections.singletonList(I18n.format("tinkers_sort.category." + hoveredTab.type + ".desc")), mouseX, mouseY, guiBook.width, guiBook.height, -1, fr);
            } else if (hoverMode) {
                GuiUtils.drawHoveringText(Collections.singletonList(currentMode.getDescription()), mouseX, mouseY, guiBook.width, guiBook.height, -1, fr);
            } else if (hoverOrder) {
                GuiUtils.drawHoveringText(Collections.singletonList(I18n.format("tinkers_sort.gui.order_tooltip")), mouseX, mouseY, guiBook.width, guiBook.height, -1, fr);
            } else if (hoverReset) {
                GuiUtils.drawHoveringText(Collections.singletonList(I18n.format("tinkers_sort.gui.reset_tooltip")), mouseX, mouseY, guiBook.width, guiBook.height, -1, fr);
            } else if (hoverClear && searchField != null && !searchField.getText().isEmpty()) {
                GuiUtils.drawHoveringText(Collections.singletonList(I18n.format("tinkers_sort.gui.clear_tooltip")), mouseX, mouseY, guiBook.width, guiBook.height, -1, fr);
            } else if (hoverSearch) {
                List<String> tooltip = java.util.Arrays.asList(
                        I18n.format("tinkers_sort.gui.search_tooltip.title"),
                        I18n.format("tinkers_sort.gui.search_tooltip.name"),
                        I18n.format("tinkers_sort.gui.search_tooltip.trait")
                );
                GuiUtils.drawHoveringText(tooltip, mouseX, mouseY, guiBook.width, guiBook.height, -1, fr);
            }
        } else {
            // Check tooltip for dropdown hovered item
            List<SortMode> modes = SortMode.getApplicableModes(target);
            int dropX = x + modeX;
            int dropY = y + modeY + modeH + 2;
            int itemH = 15;
            int dropW = modeW + 10;

            if (mouseX >= dropX && mouseX <= dropX + dropW) {
                for (int i = 0; i < modes.size(); i++) {
                    int itemY = dropY + 2 + i * itemH;
                    if (mouseY >= itemY && mouseY < itemY + itemH) {
                        GuiUtils.drawHoveringText(Collections.singletonList(modes.get(i).getDescription()), mouseX, mouseY, guiBook.width, guiBook.height, -1, fr);
                        break;
                    }
                }
            }
        }

        GlStateManager.popMatrix();
    }

    public boolean mouseClicked(GuiBook guiBook, int mouseX, int mouseY, int mouseButton) {
        if (!visible) return false;

        // 0. Handle Category Tab click
        if (isBowMaterials()) {
            for (TabRect tr : getTabRects(guiBook.mc.fontRenderer)) {
                if (isHovered(mouseX, mouseY, tr.x, tr.y, tr.w, tr.h)) {
                    this.activeBowCategory = tr.type;
                    this.isDropdownOpen = false;
                    MaterialSectionManager.navigateToBowCategory(guiBook, tr.type);
                    playClickSound(guiBook);
                    return true;
                }
            }
        }

        String target = getActiveTarget();

        // 1. Handle Dropdown click if open
        if (isDropdownOpen) {
            List<SortMode> modes = SortMode.getApplicableModes(target);
            int dropX = x + modeX;
            int dropY = y + modeY + modeH + 2;
            int itemH = 15;
            int dropH = modes.size() * itemH + 4;
            int dropW = modeW + 10;

            if (mouseX >= dropX && mouseX <= dropX + dropW && mouseY >= dropY && mouseY <= dropY + dropH) {
                int clickedIndex = (mouseY - dropY - 2) / itemH;
                if (clickedIndex >= 0 && clickedIndex < modes.size()) {
                    SortMode selected = modes.get(clickedIndex);
                    MaterialSectionManager.applySort(guiBook.book, target, selected, MaterialSectionManager.getCurrentOrder(target), MaterialSectionManager.getCurrentQuery(target));
                    if ("all".equalsIgnoreCase(this.activeBowCategory)) {
                        this.activeBowCategory = target;
                    }
                    refreshBook(guiBook, currentSection, target);
                    playClickSound(guiBook);
                }
                isDropdownOpen = false;
                return true;
            } else {
                // Clicked outside dropdown
                isDropdownOpen = false;
            }
        }

        // 2. Search Field click
        if (ModConfig.enableSearch) {
            if (isHovered(mouseX, mouseY, x + searchX, y + searchY, searchW, searchH)) {
                if (searchField != null) {
                    searchField.mouseClicked(mouseX, mouseY, mouseButton);
                }
                return true;
            } else {
                if (searchField != null && searchField.isFocused()) {
                    searchField.setFocused(false);
                }
            }

            // Clear Button click
            if (isHovered(mouseX, mouseY, x + clearX, y + clearY, clearW, clearH)) {
                if (searchField != null && !searchField.getText().isEmpty()) {
                    searchField.setText("");
                    MaterialSectionManager.applySort(guiBook.book, target, MaterialSectionManager.getCurrentMode(target), MaterialSectionManager.getCurrentOrder(target), "");
                    if ("all".equalsIgnoreCase(this.activeBowCategory)) {
                        this.activeBowCategory = target;
                    }
                    refreshBook(guiBook, currentSection, target);
                    playClickSound(guiBook);
                }
                return true;
            }
        }

        // 3. Sort Mode Button click
        if (isHovered(mouseX, mouseY, x + modeX, y + modeY, modeW, modeH)) {
            if (mouseButton == 0) { // Left click toggles dropdown
                isDropdownOpen = !isDropdownOpen;
            } else if (mouseButton == 1) { // Right click cycles mode directly
                SortMode next = MaterialSectionManager.getCurrentMode(target).next(target);
                MaterialSectionManager.applySort(guiBook.book, target, next, MaterialSectionManager.getCurrentOrder(target), MaterialSectionManager.getCurrentQuery(target));
                if ("all".equalsIgnoreCase(this.activeBowCategory)) {
                    this.activeBowCategory = target;
                }
                refreshBook(guiBook, currentSection, target);
            }
            playClickSound(guiBook);
            return true;
        }

        // 4. Sort Order Button click
        if (isHovered(mouseX, mouseY, x + orderX, y + orderY, orderW, orderH)) {
            SortOrder toggled = MaterialSectionManager.getCurrentOrder(target).toggle();
            MaterialSectionManager.applySort(guiBook.book, target, MaterialSectionManager.getCurrentMode(target), toggled, MaterialSectionManager.getCurrentQuery(target));
            if ("all".equalsIgnoreCase(this.activeBowCategory)) {
                this.activeBowCategory = target;
            }
            refreshBook(guiBook, currentSection, target);
            playClickSound(guiBook);
            return true;
        }

        // 5. Reset Button click
        if (isHovered(mouseX, mouseY, x + resetX, y + resetY, resetW, resetH)) {
            if (searchField != null) {
                searchField.setText("");
            }
            if (isBowMaterials() && "all".equalsIgnoreCase(activeBowCategory)) {
                MaterialSectionManager.resetAllBowCategories(guiBook.book);
            } else {
                MaterialSectionManager.applySort(guiBook.book, target, SortMode.DEFAULT, SortOrder.ASCENDING, "");
            }
            if ("all".equalsIgnoreCase(this.activeBowCategory)) {
                this.activeBowCategory = target;
            }
            refreshBook(guiBook, currentSection, target);
            playClickSound(guiBook);
            return true;
        }

        // If clicked anywhere within toolbar rectangle, consume event
        if (isHovered(mouseX, mouseY, x, y, TOOLBAR_WIDTH, TOOLBAR_HEIGHT)) {
            return true;
        }

        return false;
    }

    public boolean keyTyped(GuiBook guiBook, char typedChar, int keyCode) {
        if (!visible) return false;

        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (isDropdownOpen) {
                isDropdownOpen = false;
                return true;
            }
            if (searchField != null && searchField.isFocused()) {
                searchField.setFocused(false);
                return true;
            }
        }

        if (ModConfig.enableSearch && searchField != null && searchField.isFocused()) {
            String oldText = searchField.getText();
            searchField.textboxKeyTyped(typedChar, keyCode);
            String newText = searchField.getText();

            if (!oldText.equals(newText)) {
                String target = getActiveTarget();
                MaterialSectionManager.applySort(guiBook.book, target, MaterialSectionManager.getCurrentMode(target), MaterialSectionManager.getCurrentOrder(target), newText);
                if ("all".equalsIgnoreCase(this.activeBowCategory)) {
                    this.activeBowCategory = target;
                }
                refreshBook(guiBook, currentSection, target);
            }
            return true;
        }

        return false;
    }

    public static void refreshBook(GuiBook guiBook) {
        refreshBook(guiBook, null, null);
    }

    public static void refreshBook(GuiBook guiBook, String sectionName) {
        refreshBook(guiBook, sectionName, null);
    }

    public static void refreshBook(GuiBook guiBook, String sectionName, String subcategory) {
        if (guiBook == null || guiBook.book == null) return;

        MaterialSectionManager.ensureSectionsInitialized(guiBook.book);

        if (sectionName != null) {
            SectionData section = MaterialSectionManager.findSection(guiBook.book, sectionName);
            if (section != null) {
                int firstPageNum = guiBook.book.getFirstPageNumber(section, guiBook.advancementCache);
                if (firstPageNum >= 0) {
                    int offset = 0;
                    if (MaterialSectionManager.isBowCategory(sectionName)) {
                        Integer subIndex = MaterialSectionManager.getBowMaterialIconPageIndex(subcategory);
                        offset = (subIndex != null) ? subIndex : 1;
                    }
                    guiBook.openPage(firstPageNum + offset);
                    guiBook.updateScreen();
                    return;
                }
            }
        }

        int maxPage = guiBook.book.getFullPageCount(guiBook.advancementCache) - 1;
        int currentPage = guiBook.getPage_();
        if (currentPage > maxPage) {
            currentPage = Math.max(0, maxPage);
        }
        guiBook._setPage(currentPage);
        guiBook.updateScreen();
    }

    private void playClickSound(GuiBook guiBook) {
        guiBook.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private boolean isHovered(int mouseX, int mouseY, int rx, int ry, int rw, int rh) {
        return mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + rh;
    }

    private void drawBorderedRect(int x, int y, int width, int height, int borderColor, int fillColor) {
        Gui.drawRect(x, y, x + width, y + height, fillColor);
        Gui.drawRect(x, y, x + width, y + 1, borderColor);
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor);
        Gui.drawRect(x, y, x + 1, y + height, borderColor);
        Gui.drawRect(x + width - 1, y, x + width, y + height, borderColor);
    }
}
