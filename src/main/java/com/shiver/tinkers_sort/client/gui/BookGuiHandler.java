package com.shiver.tinkers_sort.client.gui;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.shiver.tinkers_sort.book.MaterialSectionManager;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.gui.book.GuiBook;

@SideOnly(Side.CLIENT)
public class BookGuiHandler {

    private final GuiSortToolbar toolbar = new GuiSortToolbar();

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiBook) {
            GuiBook guiBook = (GuiBook) event.getGui();
            toolbar.init(guiBook);
            updateToolbarVisibility(guiBook);
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiBook) {
            GuiBook guiBook = (GuiBook) event.getGui();
            updateToolbarVisibility(guiBook);
            toolbar.draw(guiBook, event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (event.getGui() instanceof GuiBook) {
            GuiBook guiBook = (GuiBook) event.getGui();
            updateToolbarVisibility(guiBook);
            if (Mouse.getEventButtonState()) {
                int mouseX = Mouse.getEventX() * guiBook.width / guiBook.mc.displayWidth;
                int mouseY = guiBook.height - Mouse.getEventY() * guiBook.height / guiBook.mc.displayHeight - 1;
                int mouseButton = Mouse.getEventButton();

                if (toolbar.mouseClicked(guiBook, mouseX, mouseY, mouseButton)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (event.getGui() instanceof GuiBook) {
            GuiBook guiBook = (GuiBook) event.getGui();
            updateToolbarVisibility(guiBook);
            if (Keyboard.getEventKeyState()) {
                char typedChar = Keyboard.getEventCharacter();
                int keyCode = Keyboard.getEventKey();

                if (toolbar.keyTyped(guiBook, typedChar, keyCode)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private void updateToolbarVisibility(GuiBook guiBook) {
        SectionData section = getCurrentSection(guiBook);
        if (isSortableSection(section)) {
            toolbar.setVisible(true);
            toolbar.setCurrentSection(section.name);
        } else {
            toolbar.setVisible(false);
        }
    }

    public static SectionData getCurrentSection(GuiBook guiBook) {
        if (guiBook == null || guiBook.book == null) return null;
        MaterialSectionManager.ensureSectionsInitialized(guiBook.book);
        int page = guiBook.getPage_();
        if (page < 0) return null; // Cover

        if (page == 0) {
            PageData p = guiBook.book.findPage(0, guiBook.advancementCache);
            return p != null ? p.parent : null;
        }

        int leftPageNum = (page - 1) * 2 + 1;
        PageData leftPage = guiBook.book.findPage(leftPageNum, guiBook.advancementCache);

        int rightPageNum = (page - 1) * 2 + 2;
        PageData rightPage = guiBook.book.findPage(rightPageNum, guiBook.advancementCache);

        // Prioritize returning a sortable section if either page on the spread belongs to one
        if (leftPage != null && isSortableSection(leftPage.parent)) {
            return leftPage.parent;
        }
        if (rightPage != null && isSortableSection(rightPage.parent)) {
            return rightPage.parent;
        }

        if (leftPage != null && leftPage.parent != null) {
            return leftPage.parent;
        }
        if (rightPage != null && rightPage.parent != null) {
            return rightPage.parent;
        }

        return null;
    }

    public static boolean isSortableSection(SectionData section) {
        if (section == null || section.name == null) return false;
        String name = section.name.toLowerCase();
        return name.equals("materials") || name.equals("bowmaterials");
    }
}

