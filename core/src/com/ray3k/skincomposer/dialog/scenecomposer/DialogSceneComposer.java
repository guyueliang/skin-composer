package com.ray3k.skincomposer.dialog.scenecomposer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.ray3k.skincomposer.Main;
import com.ray3k.skincomposer.PopTable;
import com.ray3k.skincomposer.Spinner;
import com.ray3k.skincomposer.data.DrawableData;
import com.ray3k.skincomposer.data.StyleData;
import com.ray3k.skincomposer.data.StyleProperty;
import com.ray3k.skincomposer.dialog.DialogDrawables;
import com.ray3k.skincomposer.dialog.DialogListener;
import com.ray3k.skincomposer.utils.IntPair;

public class DialogSceneComposer extends Dialog {
    public static DialogSceneComposer dialog;
    private Skin skin;
    private Main main;
    public enum View {
        LIVE, EDIT, OUTLINE
    }
    public View view;
    public DialogSceneComposerEvents events;
    public DialogSceneComposerModel model;
    private TextTooltip undoTooltip;
    private TextTooltip redoTooltip;
    private TextButton undoButton;
    private TextButton redoButton;
    private TextButton viewButton;
    public DialogSceneComposerModel.SimActor simActor;
    private Table propertiesTable;
    private Table pathTable;
    
    public DialogSceneComposer() {
        super("", Main.main.getSkin(), "scene");
        dialog = this;
        main = Main.main;
        skin = main.getSkin();
        events = new DialogSceneComposerEvents();
        model = new DialogSceneComposerModel();
        
        view = View.LIVE;
        simActor = model.root;
        
        setFillParent(true);
        
        populate();
        
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.F5) {
                    populate();
                }
                return super.keyDown(event, keycode);
            }
        });
    }
    
    private void populate() {
        getCell(getButtonTable()).space(0);
        
        var root = getContentTable();
        getCell(root).space(0);
        root.clear();
        root.defaults().reset();
        
        var table = new Table();
        table.setBackground(skin.getDrawable("scene-title-bar-ten"));
        root.add(table).growX();
    
        var label = new Label("Scene Composer", skin, "scene-title");
        table.add(label);
        
        root.row();
        table = new Table();
        table.setBackground(skin.getDrawable("scene-menu-bar-ten"));
        root.add(table).growX();
        
        var textButton = new TextButton("Import", skin, "scene-menu-button");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showImportDialog();
            }
        });
    
        textButton = new TextButton("Export", skin, "scene-menu-button");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showExportDialog();
            }
        });
    
        textButton = new TextButton("Settings", skin, "scene-menu-button");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettingsDialog();
            }
        });
    
        textButton = new TextButton("Quit", skin, "scene-menu-button");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.menuQuit();
            }
        });
        
        var image = new Image(skin, "scene-menu-divider");
        table.add(image).space(10);
    
        textButton = new TextButton("Refresh", skin, "scene-menu-button");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.menuRefresh();
            }
        });
    
        textButton = new TextButton("Clear", skin, "scene-menu-button");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.menuClear();
            }
        });
        
        image = new Image(skin, "scene-menu-divider");
        table.add(image).space(10);
    
        textButton = new TextButton("Undo", skin, "scene-menu-button");
        undoButton = textButton;
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.menuUndo();
            }
        });
        undoTooltip = new TextTooltip("", main.getTooltipManager(), skin, "scene");
    
        textButton = new TextButton("Redo", skin, "scene-menu-button");
        redoButton = textButton;
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.menuRedo();
            }
        });
        redoTooltip = new TextTooltip("", main.getTooltipManager(), skin, "scene");
    
        image = new Image(skin, "scene-menu-divider");
        table.add(image).space(10);
    
        textButton = new TextButton("", skin, "scene-menu-button");
        viewButton = textButton;
        table.add(textButton).expandX().right().space(5);
        textButton.addListener(main.getHandListener());
        textButton.addListener(menuViewListener());
    
        textButton = new TextButton("?", skin, "scene-menu-button");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.menuHelp();
            }
        });
        
        root.row();
        var top = new Table();
        top.setTouchable(Touchable.enabled);
        top.setBackground(skin.getDrawable("white"));
        
        top.add(model.preview).grow();
        
        var bottom = new Table() {
            @Override
            public float getMinHeight() {
                return 0;
            }
        };
        bottom.setTouchable(Touchable.enabled);
        bottom.setBackground(skin.getDrawable("scene-bg"));

        var splitPane = new SplitPane(top, bottom, true, skin, "scene-vertical");
        splitPane.setMinSplitAmount(0);
        root.add(splitPane).grow();
        splitPane.addListener(main.getVerticalResizeArrowListener());
    
        table = new Table();
        table.setClip(true);
        bottom.add(table).growX().minHeight(0);
        
        label = new Label("Properties", skin, "scene-title-colored");
        table.add(label);
    
        bottom.row();
        table = new Table();
        propertiesTable = table;
        var scrollPane = new ScrollPane(table, skin, "scene");
        scrollPane.setName("scroll-properties");
        scrollPane.setFlickScroll(false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setForceScroll(false,  true);
        bottom.add(scrollPane).grow();
        scrollPane.addListener(main.getScrollFocusListener());
        
        populateProperties();
    
        bottom.row();
        image = new Image(skin, "scene-path-border");
        bottom.add(image).growX();
        
        bottom.row();
        table = new Table();
        pathTable = table;
        scrollPane = new ScrollPane(table, skin, "scene");
        bottom.add(scrollPane).growX().minHeight(0).space(3);
        
        populatePath();
        
        updateMenuUndoRedo();
        updateMenuView();
    }
    
    private EventListener menuViewListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        var label = new Label("Choose a view:", skin, "scene-label-colored");
        popTable.add(label);
    
        popTable.row();
        var textButton = new TextButton("Edit", skin, "scene-med");
        popTable.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Widget highlights on mouse over. Clicks resolve widget selection.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.menuView(View.EDIT);
            }
        });
    
        popTable.row();
        textButton = new TextButton("Live", skin, "scene-med");
        popTable.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Widgets behave exactly as they do in a live libGDX project.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.menuView(View.LIVE);
            }
        });
    
        popTable.row();
        textButton = new TextButton("Outline", skin, "scene-med");
        popTable.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Debug outlines are enabled. Widget highlights on mouse over. Clicks resolve widget selection.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.menuView(View.OUTLINE);
            }
        });
        return popTableClickListener;
    }
    
    public void updateMenuView() {
        switch(view) {
            case EDIT:
                viewButton.setText("View: Edit");
                break;
            case LIVE:
                viewButton.setText("View: Live");
                break;
            case OUTLINE:
                viewButton.setText("View: Outline");
                break;
        }
    }
    
    public void updateMenuUndoRedo() {
        if (model.undoables.size > 0) {
            undoButton.setDisabled(false);
            undoTooltip.getActor().setText(model.undoables.peek().getUndoString());
            undoTooltip.getContainer().pack();
            undoButton.addListener(undoTooltip);
            undoButton.addListener(main.getHandListener());
        } else {
            undoButton.setDisabled(true);
            undoTooltip.hide();
            undoButton.removeListener(undoTooltip);
            undoButton.removeListener(main.getHandListener());
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    
        if (model.redoables.size > 0) {
            redoButton.setDisabled(false);
            redoTooltip.getActor().setText(model.redoables.peek().getRedoString());
            redoTooltip.getContainer().pack();
            redoButton.addListener(redoTooltip);
            redoButton.addListener(main.getHandListener());
        } else {
            redoButton.setDisabled(true);
            redoTooltip.hide();
            redoButton.removeListener(redoTooltip);
            redoButton.removeListener(main.getHandListener());
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }
    
    public void populateProperties() {
        var root = propertiesTable;
        root.clear();
        
        var horizontalGroup = new HorizontalGroup();
        horizontalGroup.wrap();
        horizontalGroup.align(Align.top);
        root.add(horizontalGroup).grow();
    
        if (simActor instanceof DialogSceneComposerModel.SimGroup) {
            var textButton = new TextButton("Add Table", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(rootAddTableListener());
            textButton.addListener(new TextTooltip("Creates a table with the specified number of rows and columns.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimTable) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(tableNameListener());
            textButton.addListener(new TextTooltip("Sets the name of the table to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Background", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(tableBackgroundListener());
            textButton.addListener(new TextTooltip("Sets the background drawable for the table.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Color", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(tableColorListener());
            textButton.addListener(new TextTooltip("Sets the color of the table background and of the table contents.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(tablePaddingListener());
            textButton.addListener(new TextTooltip("The padding around all of the contents inside the table.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Align", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(tableAlignListener());
            textButton.addListener(new TextTooltip("The alignment of the entire contents inside the table.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(tableResetListener());
            textButton.addListener(new TextTooltip("Resets all options back to their defaults.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(tableDeleteListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimCell) {
            var textButton = new TextButton("Set Widget", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellSetWidgetListener());
            textButton.addListener(new TextTooltip("Creates a new widget and sets it as the contents of this cell.", main.getTooltipManager(), skin, "scene"));
        
            var table = new Table();
            horizontalGroup.addActor(table);
        
            textButton = new TextButton("Add Cell to Left", skin, "scene-med");
            table.add(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    events.cellAddCellToLeft();
                }
            });
            textButton.addListener(new TextTooltip("Creates a new cell to the left of the current one.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Add Cell to Right", skin, "scene-med");
            table.add(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    events.cellAddCellToRight();
                }
            });
            textButton.addListener(new TextTooltip("Creates a new cell to the right of the current one.", main.getTooltipManager(), skin, "scene"));
        
            table = new Table();
            horizontalGroup.addActor(table);
        
            textButton = new TextButton("Add Row Above", skin, "scene-med");
            table.add(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    events.cellAddRowAbove();
                }
            });
            textButton.addListener(new TextTooltip("Adds a new row above the currently selected one.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Add Row Below", skin, "scene-med");
            table.add(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    events.cellAddRowBelow();
                }
            });
            textButton.addListener(new TextTooltip("Adds a new row below the currently selected one.", main.getTooltipManager(), skin, "scene"));
        
            table = new Table();
            horizontalGroup.addActor(table);
        
            textButton = new TextButton("Column Span", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellColSpanListener());
            textButton.addListener(new TextTooltip("Sets the column span of the current cell.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellAlignmentListener());
            textButton.addListener(new TextTooltip("Sets the alignment of the contents.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Padding / Spacing", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellPaddingSpacingListener());
            textButton.addListener(new TextTooltip("Sets the padding and/or spacing of the current cell.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Expand / Fill / Grow", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellExpandFillGrowListener());
            textButton.addListener(new TextTooltip("Sets how the current cell and its contents are sized.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Size", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellSizeListener());
            textButton.addListener(new TextTooltip("Sets the specific sizes of the contents in the cell.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Uniform", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellUniformListener());
            textButton.addListener(new TextTooltip("All cells set to to uniform = true will share the same size.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellResetListener());
            textButton.addListener(new TextTooltip("Resets all of the settings of the cell to their defaults.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Delete Cell", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(cellDeleteListener());
            textButton.addListener(new TextTooltip("Deletes the cell and its contents.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimTextButton) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonNameListener());
            textButton.addListener(new TextTooltip("Sets the name of the table to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonTextListener());
            textButton.addListener(new TextTooltip("Sets the text inside of the button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonStyleListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the text button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Checked", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonCheckedListener());
            textButton.addListener(new TextTooltip("Sets whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonDisabledListener());
            textButton.addListener(new TextTooltip("Sets whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Color", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonColorListener());
            textButton.addListener(new TextTooltip("Sets the color of the table background and of the table contents.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonPaddingListener());
            textButton.addListener(new TextTooltip("Sets the padding of the contents of the button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonResetListener());
            textButton.addListener(new TextTooltip("Resets the settings of the TextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(textButtonDeleteListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimButton) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonNameListener());
            textButton.addListener(new TextTooltip("Sets the name of the table to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonStyleListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the text button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Checked", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonCheckedListener());
            textButton.addListener(new TextTooltip("Sets whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonDisabledListener());
            textButton.addListener(new TextTooltip("Sets whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Color", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonColorListener());
            textButton.addListener(new TextTooltip("Sets the color of the table background and of the table contents.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonPaddingListener());
            textButton.addListener(new TextTooltip("Sets the padding of the contents of the button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonResetListener());
            textButton.addListener(new TextTooltip("Resets the settings of the TextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(buttonDeleteListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimImageButton) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonNameListener());
            textButton.addListener(new TextTooltip("Sets the name of the table to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonStyleListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the text button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Checked", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonCheckedListener());
            textButton.addListener(new TextTooltip("Sets whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonDisabledListener());
            textButton.addListener(new TextTooltip("Sets whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Color", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonColorListener());
            textButton.addListener(new TextTooltip("Sets the color of the table background and of the table contents.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonPaddingListener());
            textButton.addListener(new TextTooltip("Sets the padding of the contents of the button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonResetListener());
            textButton.addListener(new TextTooltip("Resets the settings of the TextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageButtonDeleteListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimImageTextButton) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonNameListener());
            textButton.addListener(new TextTooltip("Sets the name of the table to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonTextListener());
            textButton.addListener(new TextTooltip("Sets the text inside of the button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonStyleListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the text button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Checked", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonCheckedListener());
            textButton.addListener(new TextTooltip("Sets whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonDisabledListener());
            textButton.addListener(new TextTooltip("Sets whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Color", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonColorListener());
            textButton.addListener(new TextTooltip("Sets the color of the table background and of the table contents.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonPaddingListener());
            textButton.addListener(new TextTooltip("Sets the padding of the contents of the button.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonResetListener());
            textButton.addListener(new TextTooltip("Resets the settings of the TextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonDeleteListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimCheckBox) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxNameListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxStyleListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the CheckBox.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxTextListener());
            textButton.addListener(new TextTooltip("Sets the text inside of the CheckBox.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Checked", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxCheckedListener());
            textButton.addListener(new TextTooltip("Sets whether the CheckBox is checked initially.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Color", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxColorListener());
            textButton.addListener(new TextTooltip("Sets the color of the CheckBox.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxPaddingListener());
            textButton.addListener(new TextTooltip("Sets the padding of the contents of the CheckBox.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxDisabledListener());
            textButton.addListener(new TextTooltip("Sets whether the CheckBox is disabled initially.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxResetListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(checkBoxDeleteListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimImage) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageNameListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Drawable", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageDrawableListener());
            textButton.addListener(new TextTooltip("Sets the drawable to be drawn as the Image.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Scaling", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageScalingListener());
            textButton.addListener(new TextTooltip("Sets the scaling strategy of the Image when it's stretched or squeezed.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageResetListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageDeleteListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimLabel) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the Label.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the text inside of the Label.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Color", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Changes the color of the text in the Label.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Text Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the alignment of the text when the Label is larger than it's minimum size.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Ellipsis", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Enabling ellipsis allows the Label to be shortened and appends ellipsis characters (eg. \"...\")", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Wrap", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Allows the text to be wrapped to the next line if it exceeds the width of the Label.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimList) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the List.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Text List", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the text entries for the List.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimProgressBar) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the ProgressBar.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Value Settings", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the value, minimum, maximum, and increment of the ProgressBar.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Orientation", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Change the orientation of the ProgressBar.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Animation", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Change the progress animation as it increases or decreases.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Round", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Rounds the drawable positions to integers.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets whether the ProgressBar is disabled initially.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimSelectBox) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the SelectBox.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Text List", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the text entries for the SelectBox.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Max List Count", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("The maximum visible entries.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("The alignment of the text in the SelectBox.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Selected", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Choose the selected item.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Scrolling", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Choose if scrolling is enabled.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets whether the SelectBox is disabled initially.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimSlider) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the Slider.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Value Settings", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the value, minimum, maximum, and increment of the Slider.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Orientation", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Change the orientation of the Slider.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Animation", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Change the progress animation as it increases or decreases.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Round", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Rounds the drawable positions to integers.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimTextArea) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the TextArea.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the text inside of the TextArea.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Message Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("The text to be shown while there is no text, and the TextArea is not focused.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Password", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Enable password mode and set the password character.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Selection", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the cursor position and selected range.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the alignment of the typed text.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Focus Traversal", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Enable traversal to the next TextArea by using the TAB key.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Max Length", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the maximum length of the typed text.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Preferred Rows", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the preferred number of lines.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets whether the TextArea is disabled initially.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimTextField) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the TextField.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the text inside of the TextField.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the text inside of the TextField.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Message Text", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("The text to be shown while there is no text, and the TextField is not focused.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Password", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Enable password mode and set the password character.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Selection", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the cursor position and selected range.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the alignment of the typed text.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Focus Traversal", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Enable traversal to the next TextField by using the TAB key.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Max Length", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the maximum length of the typed text.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Disabled", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets whether the TextField is disabled initially.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimTouchPad) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the TouchPad.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Dead Zone", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Change the dead zone that does not react to user input.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reset on Touch Up", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Enable the reset of the touch pad position upon the release of the widget.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimContainer) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Set Widget", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the widget assigned to this Container.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Background", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the background of the Container.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Fill", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the fill of the widget.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Size", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the size of the widget.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the padding of the widget.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the alignment of the widget.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimHorizontalGroup) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Add Child", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Adds a widget to the HorizontalGroup", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Expand", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the widgets to expand to the available space", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Fill", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the fill amount of the widgets.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the padding of the widgets.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Space", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the space between the widgets.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Wrap", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set whether widgets will wrap to the next line when the width is decreased.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the alignment of the widgets", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reverse", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Reverse the display order of the widgets.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimScrollPane) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the ScrollPane.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Set Widget", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Knobs", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Scrolling", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimSplitPane) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the style that controls the appearance of the SplitPane.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Set First Widget", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the first widget applied to the SplitPane", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Set Second Widget", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the second widget applied to the SplitPane", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Orientation", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the orientation of the SplitPane.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Split", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the split, splitMin, and splitMax values.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimStack) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Add Child", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Add a child to the Stack.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimNode) {
            var textButton = new TextButton("Set Widget", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the widget applied to this Node.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Add Node", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Adds a new child Node to this Node.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Icon", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Select the Drawable applied as an icon to the Node.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Options", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Change the expanded and selected values.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimTree) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the Tree.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Add Node", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Spacing", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        } else if (simActor instanceof DialogSceneComposerModel.SimVerticalGroup) {
            var textButton = new TextButton("Name", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the name of the widget to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Style", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Sets the style that controls the appearance of the VerticalGroup.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Add Child", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Adds a widget to the VerticalGroup.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Expand", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the widgets to expand to the available space.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Fill", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the fill amount of the widgets.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Padding", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the padding of the widgets.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Space", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the space between the widgets.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Wrap", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set whether widgets will wrap to the next line when the height is decreased.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Alignment", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Set the alignment of the widgets", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Reverse", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Reverse the display order of the widgets.", main.getTooltipManager(), skin, "scene"));
            
            textButton = new TextButton("Reset", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(imageTextButtonResetListener());
            textButton.addListener(new TextTooltip("Resets the settings of the widget to its defaults.", main.getTooltipManager(), skin, "scene"));
    
            textButton = new TextButton("Delete", skin, "scene-med");
            horizontalGroup.addActor(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new TextTooltip("Removes this widget from its parent.", main.getTooltipManager(), skin, "scene"));
        }
    }
    
    private EventListener buttonNameListener() {
        var simButton = (DialogSceneComposerModel.SimButton) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Name:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simButton.name);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the button to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.buttonName(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener buttonStyleListener() {
        var simButton = (DialogSceneComposerModel.SimButton) simActor;
        var popTableClickListener = new StyleSelectorPopTable(TextButton.class, simButton.style == null ? "default" : simButton.style.name) {
            @Override
            public void accepted(StyleData styleData) {
                events.buttonStyle(styleData);
            }
        };
        
        return popTableClickListener;
    }
    
    private EventListener buttonCheckedListener() {
        var simButton = (DialogSceneComposerModel.SimButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Checked:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simButton.checked ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simButton.checked);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.buttonChecked(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener buttonDisabledListener() {
        var simButton = (DialogSceneComposerModel.SimButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Disabled:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simButton.disabled ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simButton.disabled);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.buttonDisabled(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener buttonColorListener() {
        var simButton = (DialogSceneComposerModel.SimButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Color:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var imageButton = new ImageButton(skin, "scene-color");
                imageButton.getImage().setColor(simButton.color == null ? Color.WHITE : simButton.color.color);
                popTable.add(imageButton).minWidth(100);
                imageButton.addListener(main.getHandListener());
                imageButton.addListener(new TextTooltip("Select the color of the button.", main.getTooltipManager(), skin, "scene"));
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogColors(new StyleProperty(), (colorData, pressedCancel) -> {
                            if (!pressedCancel) {
                                events.buttonColor(colorData);
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
                            
                            }
                            
                            @Override
                            public void closed() {
                            
                            }
                        });
                    }
                });
                
                popTable.row();
                label = new Label(simButton.color == null ? "white" : simButton.color.getName(), skin, "scene-label-colored");
                popTable.add(label);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener buttonPaddingListener() {
        var simButton = (DialogSceneComposerModel.SimButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner padLeft = popTable.findActor("pad-left");
                        Spinner padRight = popTable.findActor("pad-right");
                        Spinner padTop = popTable.findActor("pad-top");
                        Spinner padBottom = popTable.findActor("pad-bottom");
                        events.buttonPadding((float) padLeft.getValue(), (float) padRight.getValue(), (float) padTop.getValue(), (float) padBottom.getValue());
                    }
                };
                
                var label = new Label("Padding:", skin, "scene-label-colored");
                popTable.add(label).colspan(2);
                
                popTable.row();
                popTable.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                popTable.add(label);
                
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-left");
                spinner.setValue(simButton.padLeft);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the left of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Right:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-right");
                spinner.setValue(simButton.padRight);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the right of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Top:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-top");
                spinner.setValue(simButton.padTop);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the top of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-bottom");
                spinner.setValue(simButton.padBottom);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the bottom of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener buttonResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to reset this Button?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets the settings of the Button to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.buttonReset();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener buttonDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to delete this Button?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Removes this Button from its parent.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.buttonDelete();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonNameListener() {
        var simImageButton = (DialogSceneComposerModel.SimImageButton) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Name:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simImageButton.name);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the button to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.imageButtonName(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonStyleListener() {
        var simImageButton = (DialogSceneComposerModel.SimImageButton) simActor;
        var popTableClickListener = new StyleSelectorPopTable(TextButton.class, simImageButton.style == null ? "default" : simImageButton.style.name) {
            @Override
            public void accepted(StyleData styleData) {
                events.imageButtonStyle(styleData);
            }
        };
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonCheckedListener() {
        var simImageButton = (DialogSceneComposerModel.SimImageButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Checked:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simImageButton.checked ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simImageButton.checked);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.imageButtonChecked(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonDisabledListener() {
        var simImageButton = (DialogSceneComposerModel.SimImageButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Disabled:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simImageButton.disabled ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simImageButton.disabled);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.imageButtonDisabled(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonColorListener() {
        var simImageButton = (DialogSceneComposerModel.SimImageButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Color:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var imageButton = new ImageButton(skin, "scene-color");
                imageButton.getImage().setColor(simImageButton.color == null ? Color.WHITE : simImageButton.color.color);
                popTable.add(imageButton).minWidth(100);
                imageButton.addListener(main.getHandListener());
                imageButton.addListener(new TextTooltip("Select the color of the button.", main.getTooltipManager(), skin, "scene"));
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogColors(new StyleProperty(), (colorData, pressedCancel) -> {
                            if (!pressedCancel) {
                                events.imageButtonColor(colorData);
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
                            
                            }
                            
                            @Override
                            public void closed() {
                            
                            }
                        });
                    }
                });
                
                popTable.row();
                label = new Label(simImageButton.color == null ? "white" : simImageButton.color.getName(), skin, "scene-label-colored");
                popTable.add(label);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonPaddingListener() {
        var simImageButton = (DialogSceneComposerModel.SimImageButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner padLeft = popTable.findActor("pad-left");
                        Spinner padRight = popTable.findActor("pad-right");
                        Spinner padTop = popTable.findActor("pad-top");
                        Spinner padBottom = popTable.findActor("pad-bottom");
                        events.imageButtonPadding((float) padLeft.getValue(), (float) padRight.getValue(), (float) padTop.getValue(), (float) padBottom.getValue());
                    }
                };
                
                var label = new Label("Padding:", skin, "scene-label-colored");
                popTable.add(label).colspan(2);
                
                popTable.row();
                popTable.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                popTable.add(label);
                
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-left");
                spinner.setValue(simImageButton.padLeft);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the left of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Right:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-right");
                spinner.setValue(simImageButton.padRight);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the right of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Top:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-top");
                spinner.setValue(simImageButton.padTop);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the top of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-bottom");
                spinner.setValue(simImageButton.padBottom);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the bottom of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to reset this button?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets the settings of the TextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.imageButtonReset();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener imageButtonDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to delete this ImageButton?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Removes this TextButton from its parent.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.imageButtonDelete();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonNameListener() {
        var simImageTextButton = (DialogSceneComposerModel.SimImageTextButton) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Name:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simImageTextButton.name);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the button to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.imageTextButtonName(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonTextListener() {
        var simImageTextButton = (DialogSceneComposerModel.SimImageTextButton) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Text:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simImageTextButton.text);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The text inside of the button.", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.imageTextButtonText(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonStyleListener() {
        var imageTextButton = (DialogSceneComposerModel.SimImageTextButton) simActor;
        var popTableClickListener = new StyleSelectorPopTable(TextButton.class, imageTextButton.style == null ? "default" : imageTextButton.style.name) {
            @Override
            public void accepted(StyleData styleData) {
                events.imageTextButtonStyle(styleData);
            }
        };
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonCheckedListener() {
        var simImageTextButton = (DialogSceneComposerModel.SimImageTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Checked:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simImageTextButton.checked ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simImageTextButton.checked);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.imageTextButtonChecked(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonDisabledListener() {
        var simImageTextButton = (DialogSceneComposerModel.SimImageTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Disabled:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simImageTextButton.disabled ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simImageTextButton.disabled);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.imageTextButtonDisabled(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonColorListener() {
        var simImageTextButton = (DialogSceneComposerModel.SimImageTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Color:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var imageButton = new ImageButton(skin, "scene-color");
                imageButton.getImage().setColor(simImageTextButton.color == null ? Color.WHITE : simImageTextButton.color.color);
                popTable.add(imageButton).minWidth(100);
                imageButton.addListener(main.getHandListener());
                imageButton.addListener(new TextTooltip("Select the color of the button.", main.getTooltipManager(), skin, "scene"));
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogColors(new StyleProperty(), (colorData, pressedCancel) -> {
                            if (!pressedCancel) {
                                events.imageTextButtonColor(colorData);
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
                            
                            }
                            
                            @Override
                            public void closed() {
                            
                            }
                        });
                    }
                });
                
                popTable.row();
                label = new Label(simImageTextButton.color == null ? "white" : simImageTextButton.color.getName(), skin, "scene-label-colored");
                popTable.add(label);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonPaddingListener() {
        var simImageTextButton = (DialogSceneComposerModel.SimImageTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner padLeft = popTable.findActor("pad-left");
                        Spinner padRight = popTable.findActor("pad-right");
                        Spinner padTop = popTable.findActor("pad-top");
                        Spinner padBottom = popTable.findActor("pad-bottom");
                        events.imageTextButtonPadding((float) padLeft.getValue(), (float) padRight.getValue(), (float) padTop.getValue(), (float) padBottom.getValue());
                    }
                };
                
                var label = new Label("Padding:", skin, "scene-label-colored");
                popTable.add(label).colspan(2);
                
                popTable.row();
                popTable.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                popTable.add(label);
                
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-left");
                spinner.setValue(simImageTextButton.padLeft);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the left of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Right:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-right");
                spinner.setValue(simImageTextButton.padRight);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the right of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Top:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-top");
                spinner.setValue(simImageTextButton.padTop);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the top of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-bottom");
                spinner.setValue(simImageTextButton.padBottom);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the bottom of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to reset this cell?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets the settings of the ImageTextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.imageTextButtonReset();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener imageTextButtonDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to delete this textButton?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Removes this ImageTextButton from its parent.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.imageTextButtonDelete();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener textButtonNameListener() {
        var simTextButton = (DialogSceneComposerModel.SimTextButton) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Name:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simTextButton.name);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the button to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.textButtonName(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener textButtonTextListener() {
        var simTextButton = (DialogSceneComposerModel.SimTextButton) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Text:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simTextButton.text);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The text inside of the button.", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.textButtonText(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener textButtonStyleListener() {
        var simTextButton = (DialogSceneComposerModel.SimTextButton) simActor;
        var popTableClickListener = new StyleSelectorPopTable(TextButton.class, simTextButton.style == null ? "default" : simTextButton.style.name) {
            @Override
            public void accepted(StyleData styleData) {
                events.textButtonStyle(styleData);
            }
        };
        
        return popTableClickListener;
    }
    
    private EventListener textButtonCheckedListener() {
        var simTextButton = (DialogSceneComposerModel.SimTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Checked:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simTextButton.checked ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simTextButton.checked);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is checked initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.textButtonChecked(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener textButtonDisabledListener() {
        var simTextButton = (DialogSceneComposerModel.SimTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Disabled:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simTextButton.disabled ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simTextButton.disabled);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the button is disabled initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.textButtonDisabled(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener textButtonColorListener() {
        var simTextButton = (DialogSceneComposerModel.SimTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Color:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var imageButton = new ImageButton(skin, "scene-color");
                imageButton.getImage().setColor(simTextButton.color == null ? Color.WHITE : simTextButton.color.color);
                popTable.add(imageButton).minWidth(100);
                imageButton.addListener(main.getHandListener());
                imageButton.addListener(new TextTooltip("Select the color of the button.", main.getTooltipManager(), skin, "scene"));
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogColors(new StyleProperty(), (colorData, pressedCancel) -> {
                            if (!pressedCancel) {
                                events.textButtonColor(colorData);
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
                            
                            }
                            
                            @Override
                            public void closed() {
                            
                            }
                        });
                    }
                });
                
                popTable.row();
                label = new Label(simTextButton.color == null ? "white" : simTextButton.color.getName(), skin, "scene-label-colored");
                popTable.add(label);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener textButtonPaddingListener() {
        var simTextButton = (DialogSceneComposerModel.SimTextButton) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner padLeft = popTable.findActor("pad-left");
                        Spinner padRight = popTable.findActor("pad-right");
                        Spinner padTop = popTable.findActor("pad-top");
                        Spinner padBottom = popTable.findActor("pad-bottom");
                        events.textButtonPadding((float) padLeft.getValue(), (float) padRight.getValue(), (float) padTop.getValue(), (float) padBottom.getValue());
                    }
                };
                
                var label = new Label("Padding:", skin, "scene-label-colored");
                popTable.add(label).colspan(2);
                
                popTable.row();
                popTable.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                popTable.add(label);
                
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-left");
                spinner.setValue(simTextButton.padLeft);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the left of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Right:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-right");
                spinner.setValue(simTextButton.padRight);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the right of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Top:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-top");
                spinner.setValue(simTextButton.padTop);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the top of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-bottom");
                spinner.setValue(simTextButton.padBottom);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the bottom of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener textButtonResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to reset this TextButton?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets the settings of the TextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.textButtonReset();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener textButtonDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to delete this textButton?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Removes this TextButton from its parent.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.textButtonDelete();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener tableNameListener() {
        var simTable = (DialogSceneComposerModel.SimTable) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var label = new Label("Name:", skin, "scene-label-colored");
                popTable.add(label);
    
                popTable.row();
                var textField = new TextField("", skin, "scene");
                textField.setText(simTable.name);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the table to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableName(textField.getText());
                    }
                });
    
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener tableBackgroundListener() {
        var simTable = (DialogSceneComposerModel.SimTable) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var label = new Label("Background:", skin, "scene-label-colored");
                popTable.add(label);
    
                popTable.row();
                var stack = new Stack();
                popTable.add(stack).minSize(100).maxSize(300).grow();
                var background = new Image(skin, "scene-tile-ten");
                stack.add(background);
                Image image;
                if (simTable.background != null) {
                    image = new Image(main.getAtlasData().drawablePairs.get(simTable.background));
                } else {
                    image = new Image((Drawable) null);
                }
                stack.add(image);
    
                popTable.row();
                var textButton = new TextButton("Select Drawable", skin, "scene-small");
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("The background drawable for the table.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogDrawables(true, new DialogDrawables.DialogDrawablesListener() {
                            @Override
                            public void confirmed(DrawableData drawable, DialogDrawables dialog) {
                                events.tableBackground(drawable);
                                image.setDrawable(main.getAtlasData().drawablePairs.get(drawable));
                            }
                
                            @Override
                            public void emptied(DialogDrawables dialog) {
                                events.tableBackground(null);
                                image.setDrawable(null);
                            }
                
                            @Override
                            public void cancelled(DialogDrawables dialog) {
                    
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
                    
                            }
                
                            @Override
                            public void closed() {
                    
                            }
                        });
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener tableColorListener() {
        var simTable = (DialogSceneComposerModel.SimTable) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var label = new Label("Color:", skin, "scene-label-colored");
                popTable.add(label);
    
                popTable.row();
                var imageButton = new ImageButton(skin, "scene-color");
                imageButton.getImage().setColor(simTable.color == null ? Color.WHITE : simTable.color.color);
                popTable.add(imageButton).minWidth(100);
                imageButton.addListener(main.getHandListener());
                imageButton.addListener(new TextTooltip("Select the color of the table background and of the table contents.", main.getTooltipManager(), skin, "scene"));
    
                popTable.row();
                var colorLabel = new Label(simTable.color == null ? "No Color" : simTable.color.getName(), skin, "scene-label-colored");
                popTable.add(colorLabel);
    
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogColors(new StyleProperty(), (colorData, pressedCancel) -> {
                            if (!pressedCancel) {
                                events.tableColor(colorData);
                                imageButton.getImage().setColor(colorData == null ? Color.WHITE : colorData.color);
                                colorLabel.setText(colorData == null ? "No Color" : colorData.getName());
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
                    
                            }
                
                            @Override
                            public void closed() {
                    
                            }
                        });
                    }
                });
            }
        };
        
        popTableClickListener.update();
    
        return popTableClickListener;
    }
    
    private EventListener tablePaddingListener() {
        var simTable = (DialogSceneComposerModel.SimTable) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner padLeft = popTable.findActor("pad-left");
                        Spinner padRight = popTable.findActor("pad-right");
                        Spinner padTop = popTable.findActor("pad-top");
                        Spinner padBottom = popTable.findActor("pad-bottom");
                        events.tablePadding((float) padLeft.getValue(), (float)  padRight.getValue(), (float)  padTop.getValue(), (float)  padBottom.getValue());
                    }
                };
    
                var label = new Label("Padding:", skin, "scene-label-colored");
                popTable.add(label).colspan(2);
    
                popTable.row();
                popTable.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                popTable.add(label);
    
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-left");
                spinner.setValue(simTable.padLeft);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the left of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                popTable.row();
                label = new Label("Right:", skin, "scene-label-colored");
                popTable.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-right");
                spinner.setValue(simTable.padRight);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the right of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                popTable.row();
                label = new Label("Top:", skin, "scene-label-colored");
                popTable.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-top");
                spinner.setValue(simTable.padTop);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the top of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                popTable.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                popTable.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-bottom");
                spinner.setValue(simTable.padBottom);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the bottom of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener tableAlignListener() {
        var simTable = (DialogSceneComposerModel.SimTable) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var table = new Table();
                popTable.add(table);
    
                var label = new Label("Alignment:", skin, "scene-label-colored");
                table.add(label).colspan(3);
    
                table.row();
                table.defaults().space(10).left().uniformX();
                var buttonGroup = new ButtonGroup<ImageTextButton>();
                var imageTextButton = new ImageTextButton("Top-Left", skin, "scene-checkbox-colored");
                var topLeft = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the top left.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.topLeft);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Top", skin, "scene-checkbox-colored");
                var top = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the top center.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.top);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Top-Right", skin, "scene-checkbox-colored");
                var topRight = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the top right.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.topRight);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                table.row();
                imageTextButton = new ImageTextButton("Left", skin, "scene-checkbox-colored");
                var left = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the middle left.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.left);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Center", skin, "scene-checkbox-colored");
                var center = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the center.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.center);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Right", skin, "scene-checkbox-colored");
                var right = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the middle right.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.right);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                table.row();
                imageTextButton = new ImageTextButton("Bottom-Left", skin, "scene-checkbox-colored");
                var bottomLeft = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the bottom left.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.bottomLeft);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Bottom", skin, "scene-checkbox-colored");
                var bottom = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the bottom center.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.bottom);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Bottom-Right", skin, "scene-checkbox-colored");
                var bottomRight = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents to the bottom right.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.tableAlignment(Align.bottomRight);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                switch (simTable.alignment) {
                    case Align.topLeft:
                        topLeft.setChecked(true);
                        break;
                    case Align.top:
                        top.setChecked(true);
                        break;
                    case Align.topRight:
                        topRight.setChecked(true);
                        break;
                    case Align.right:
                        right.setChecked(true);
                        break;
                    case Align.bottomRight:
                        bottomRight.setChecked(true);
                        break;
                    case Align.bottom:
                        bottom.setChecked(true);
                        break;
                    case Align.bottomLeft:
                        bottomLeft.setChecked(true);
                        break;
                    case Align.left:
                        left.setChecked(true);
                        break;
                    case Align.center:
                        center.setChecked(true);
                        break;
                }
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener tableResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to reset this table?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets all options back to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.tableReset();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener tableDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to delete this table?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Removes this table from its parent.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.tableDelete();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener rootAddTableListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin, "dark");
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("New Table:", skin, "scene-label");
        popTable.add(label);
        label.addListener(new TextTooltip("Creates a base Table and adds it directly to the stage. This will serve as the basis for the rest of your UI layout and will fill the entire screen.", main.getTooltipManager(), skin, "scene"));
    
        popTable.row();
        var table = new Table();
        popTable.add(table);
        
        table.pad(10).padTop(0);
        var buttons = new Button[6][6];
        for (int j = 0; j < 6; j++) {
            table.row();
            for (int i = 0; i < 6; i++) {
                var textButton = new Button(skin, "scene-table");
                textButton.setProgrammaticChangeEvents(false);
                textButton.setUserObject(new IntPair(i, j));
                table.add(textButton);
                buttons[i][j] = textButton;
                int testI = i, testJ = j;
                textButton.addListener(main.getHandListener());
                textButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        popTable.hide();
                        var intPair = (IntPair) textButton.getUserObject();
                        events.rootAddTable(intPair.x + 1, intPair.y + 1);
                    }
                });
                textButton.addListener(new InputListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        var intPair = (IntPair) textButton.getUserObject();
                        for (int y1 = 0; y1 < 6; y1++) {
                            for (int x1 = 0; x1 < 6; x1++) {
                                buttons[x1][y1].setChecked(y1 <= intPair.y && x1 <= intPair.x);
                            }
                        }
                        textButton.setChecked(true);
                    }
                });
            }
        }
        
        table.setTouchable(Touchable.enabled);
        table.addListener(new InputListener() {
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                for (int y1 = 0; y1 < 6; y1++) {
                    for (int x1 = 0; x1 < 6; x1++) {
                        buttons[x1][y1].setChecked(false);
                    }
                }
            }
        });
    
        return popTableClickListener;
    }
    
    private EventListener cellDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var table = popTableClickListener.getPopTable();
    
        var label = new Label("Are you sure you want to delete this cell?", skin, "scene-label-colored");
        table.add(label);
    
        table.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        table.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Deletes the cell and its contents.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                table.hide();
                events.cellDelete();
            }
        });
    
        return popTableClickListener;
    }
    
    private EventListener cellResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var table = popTableClickListener.getPopTable();
    
        var label = new Label("Are you sure you want to reset this cell?", skin, "scene-label-colored");
        table.add(label);
    
        table.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        table.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets all of the settings of the cell to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                table.hide();
                events.cellReset();
            }
        });
    
        return popTableClickListener;
    }
    
    private EventListener cellUniformListener() {
        var simCell = (DialogSceneComposerModel.SimCell) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        ImageTextButton uniformX = popTable.findActor("uniform-x");
                        ImageTextButton uniformY = popTable.findActor("uniform-y");
                        events.cellUniform(uniformX.isChecked(), uniformY.isChecked());
                    }
                };
    
                var table = new Table();
                popTable.add(table);
    
                table.defaults().left().spaceRight(5);
                var imageTextButton = new ImageTextButton("Uniform X", skin, "scene-checkbox-colored");
                imageTextButton.setName("uniform-x");
                imageTextButton.setChecked(simCell.uniformX);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("All cells with Uniform X will share the same width.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(changeListener);
    
                imageTextButton = new ImageTextButton("Uniform Y", skin, "scene-checkbox-colored");
                imageTextButton.setName("uniform-y");
                imageTextButton.setChecked(simCell.uniformY);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("All cells with Uniform Y will share the same height.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
    
        return popTableClickListener;
    }
    
    private EventListener cellSizeListener() {
        var simCell = (DialogSceneComposerModel.SimCell) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var table = new Table();
                popTable.add(table);
    
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner minimumWidth = popTable.findActor("minimum-width");
                        Spinner minimumHeight = popTable.findActor("minimum-height");
                        Spinner maximumWidth = popTable.findActor("maximum-width");
                        Spinner maximumHeight = popTable.findActor("maximum-height");
                        Spinner preferredWidth = popTable.findActor("preferred-width");
                        Spinner preferredHeight = popTable.findActor("preferred-height");
                        events.cellSize((float) minimumWidth.getValue(), (float) minimumHeight.getValue(), (float) maximumWidth.getValue(), (float) maximumHeight.getValue(), (float) preferredWidth.getValue(), (float) preferredHeight.getValue());
                    }
                };
    
                var label = new Label("Minimum:", skin, "scene-label-colored");
                table.add(label).colspan(2);
    
                table.row();
                table.defaults().right().spaceRight(5);
                label = new Label("Width:", skin, "scene-label-colored");
                table.add(label);
    
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("minimum-width");
                spinner.setValue(simCell.minWidth);
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The minimum width of the contents of the cell.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Height:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("minimum-height");
                spinner.setValue(simCell.minHeight);
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The minimum height of the contents of the cell.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                var image = new Image(skin, "scene-menu-divider");
                popTable.add(image).space(10).growY();
    
                table = new Table();
                popTable.add(table);
    
                label = new Label("Maximum:", skin, "scene-label-colored");
                table.add(label).colspan(2);
    
                table.row();
                table.defaults().right().spaceRight(5);
                label = new Label("Width:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("maximum-width");
                spinner.setValue(simCell.maxWidth);
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The maximum width of the contents of the cell.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Height:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("maximum-height");
                spinner.setValue(simCell.maxHeight);
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The maximum height of the contents of the cell.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                image = new Image(skin, "scene-menu-divider");
                popTable.add(image).space(10).growY();
    
                table = new Table();
                popTable.add(table);
    
                label = new Label("Preferred:", skin, "scene-label-colored");
                table.add(label).colspan(2);
    
                table.row();
                table.defaults().right().spaceRight(5);
                label = new Label("Width:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("preferred-width");
                spinner.setValue(simCell.preferredWidth);
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The preferred width of the contents of the cell.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Height:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("preferred-height");
                spinner.setValue(simCell.preferredHeight);
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The preferred height of the contents of the cell.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
    
        return popTableClickListener;
    }
    
    private EventListener cellAlignmentListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var simCell = (DialogSceneComposerModel.SimCell) simActor;
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var table = new Table();
                popTable.add(table);
    
                var label = new Label("Alignment:", skin, "scene-label-colored");
                table.add(label).colspan(3);
    
                table.row();
                table.defaults().space(10).left().uniformX();
                var buttonGroup = new ButtonGroup<ImageTextButton>();
                var imageTextButton = new ImageTextButton("Top-Left", skin, "scene-checkbox-colored");
                var topLeft = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the top left.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.topLeft);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Top", skin, "scene-checkbox-colored");
                var top = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the top center.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.top);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Top-Right", skin, "scene-checkbox-colored");
                var topRight = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the top right.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.topRight);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                table.row();
                imageTextButton = new ImageTextButton("Left", skin, "scene-checkbox-colored");
                var left = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the middle left.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.left);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Center", skin, "scene-checkbox-colored");
                var center = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the center.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.center);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Right", skin, "scene-checkbox-colored");
                var right = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the middle right.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.right);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                table.row();
                imageTextButton = new ImageTextButton("Bottom-Left", skin, "scene-checkbox-colored");
                var bottomLeft = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the bottom left.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.bottomLeft);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Bottom", skin, "scene-checkbox-colored");
                var bottom = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the bottom center.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.bottom);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                imageTextButton = new ImageTextButton("Bottom-Right", skin, "scene-checkbox-colored");
                var bottomRight = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the cell to the bottom right.", main.getTooltipManager(), skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellAlignment(Align.bottomRight);
                    }
                });
                buttonGroup.add(imageTextButton);
    
                switch (simCell.alignment) {
                    case Align.topLeft:
                        topLeft.setChecked(true);
                        break;
                    case Align.top:
                        top.setChecked(true);
                        break;
                    case Align.topRight:
                        topRight.setChecked(true);
                        break;
                    case Align.right:
                        right.setChecked(true);
                        break;
                    case Align.bottomRight:
                        bottomRight.setChecked(true);
                        break;
                    case Align.bottom:
                        bottom.setChecked(true);
                        break;
                    case Align.bottomLeft:
                        bottomLeft.setChecked(true);
                        break;
                    case Align.left:
                        left.setChecked(true);
                        break;
                    case Align.center:
                        center.setChecked(true);
                        break;
                }
            }
        };
        
        popTableClickListener.update();
    
        return popTableClickListener;
    }
    
    private EventListener cellExpandFillGrowListener() {
        var simCell = (DialogSceneComposerModel.SimCell) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var expandX = new ImageTextButton("Expand X", skin, "scene-checkbox-colored");
                var expandY = new ImageTextButton("Expand Y", skin, "scene-checkbox-colored");
                var fillX = new ImageTextButton("Fill X", skin, "scene-checkbox-colored");
                var fillY = new ImageTextButton("Fill Y", skin, "scene-checkbox-colored");
                var growX = new ImageTextButton("Grow X", skin, "scene-checkbox-colored");
                var growY = new ImageTextButton("Grow Y", skin, "scene-checkbox-colored");
    
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellExpandFillGrow(expandX.isChecked(), expandY.isChecked(), fillX.isChecked(), fillY.isChecked(), growX.isChecked(), growY.isChecked());
                    }
                };
    
                var table = new Table();
                popTable.add(table);
    
                table.defaults().left().spaceRight(5);
                expandX.setChecked(simCell.expandX);
                expandX.setProgrammaticChangeEvents(false);
                table.add(expandX);
                expandX.addListener(main.getHandListener());
                expandX.addListener(new TextTooltip("Expands the width of the cell to the available space.", main.getTooltipManager(), skin, "scene"));
                expandX.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (expandX.isChecked() && fillX.isChecked()) {
                            growX.setChecked(true);
                        } else {
                            growX.setChecked(false);
                        }
                    }
                });
                expandX.addListener(changeListener);

                expandY.setChecked(simCell.expandY);
                expandY.setProgrammaticChangeEvents(false);
                table.add(expandY);
                expandY.addListener(main.getHandListener());
                expandY.addListener(new TextTooltip("Expands the height of the cell to the available space.", main.getTooltipManager(), skin, "scene"));
                expandY.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (expandY.isChecked() && fillY.isChecked()) {
                            growY.setChecked(true);
                        } else {
                            growY.setChecked(false);
                        }
                    }
                });
                expandY.addListener(changeListener);
    
                table.row();
                fillX.setChecked(simCell.fillX);
                fillX.setProgrammaticChangeEvents(false);
                table.add(fillX);
                fillX.addListener(main.getHandListener());
                fillX.addListener(new TextTooltip("Stretches the contents to fill the width of the cell.", main.getTooltipManager(), skin, "scene"));
                fillX.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (expandX.isChecked() && fillX.isChecked()) {
                            growX.setChecked(true);
                        } else {
                            growX.setChecked(false);
                        }
                    }
                });
                fillX.addListener(changeListener);
    
                fillY.setChecked(simCell.fillY);
                fillY.setProgrammaticChangeEvents(false);
                table.add(fillY);
                fillY.addListener(main.getHandListener());
                fillY.addListener(new TextTooltip("Stretches the contents to fill the height of the cell.", main.getTooltipManager(), skin, "scene"));
                fillY.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (expandY.isChecked() && fillY.isChecked()) {
                            growY.setChecked(true);
                        } else {
                            growY.setChecked(false);
                        }
                    }
                });
                fillY.addListener(changeListener);
    
                table.row();
                growX.setChecked(simCell.growX);
                growX.setProgrammaticChangeEvents(false);
                table.add(growX);
                growX.addListener(main.getHandListener());
                growX.addListener(new TextTooltip("Sets the cell to expand and fill across the available width.", main.getTooltipManager(), skin, "scene"));
                growX.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        expandX.setChecked(growX.isChecked());
                        fillX.setChecked(growX.isChecked());
                    }
                });
                growX.addListener(changeListener);
    
                growY.setChecked(simCell.growY);
                growY.setProgrammaticChangeEvents(false);
                table.add(growY);
                growY.addListener(main.getHandListener());
                growY.addListener(new TextTooltip("Sets the cell to expand and fill across the available height.", main.getTooltipManager(), skin, "scene"));
                growY.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        expandY.setChecked(growY.isChecked());
                        fillY.setChecked(growY.isChecked());
                    }
                });
                growY.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener cellColSpanListener() {
        var simCell = (DialogSceneComposerModel.SimCell) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var table = new Table();
                popTable.add(table);
                
                var label = new Label("Column Span:", skin, "scene-label-colored");
                table.add(label).colspan(2);
                
                table.row();
                
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.colSpan);
                spinner.setMinimum(1);
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The column span of the cell.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.cellColSpan(spinner.getValueAsInt());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener cellPaddingSpacingListener() {
        var simCell = (DialogSceneComposerModel.SimCell) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
    
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner paddingLeft = popTable.findActor("padding-left");
                        Spinner paddingRight = popTable.findActor("padding-right");
                        Spinner paddingTop = popTable.findActor("padding-top");
                        Spinner paddingBottom = popTable.findActor("padding-bottom");
                        Spinner spacingLeft = popTable.findActor("spacing-left");
                        Spinner spacingRight = popTable.findActor("spacing-right");
                        Spinner spacingTop = popTable.findActor("spacing-top");
                        Spinner spacingBottom = popTable.findActor("spacing-bottom");
            
                        events.cellPaddingSpacing((float) paddingLeft.getValue(), (float) paddingRight.getValue(), (float) paddingTop.getValue(), (float) paddingBottom.getValue(), (float) spacingLeft.getValue(), (float) spacingRight.getValue(), (float) spacingTop.getValue(), (float) spacingBottom.getValue());
                    }
                };
    
                var table = new Table();
                popTable.add(table);
    
                var label = new Label("Padding:", skin, "scene-label-colored");
                table.add(label).colspan(2);
    
                table.row();
                table.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                table.add(label);
    
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.padLeft);
                spinner.setName("padding-left");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding to the left of the cell. Stacks with other cell padding.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Right:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.padRight);
                spinner.setName("padding-right");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding to the right of the cell. Stacks with other cell padding.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Top:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.padTop);
                spinner.setName("padding-top");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding to the top of the cell. Stacks with other cell padding.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.padBottom);
                spinner.setName("padding-bottom");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding to the bottom of the cell. Stacks with other cell padding.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                var image = new Image(skin, "scene-menu-divider");
                popTable.add(image).space(10).growY();
    
                table = new Table();
                popTable.add(table);
    
                label = new Label("Spacing:", skin, "scene-label-colored");
                table.add(label).colspan(2);
    
                table.row();
                table.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.spaceLeft);
                spinner.setName("spacing-left");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The spacing to the left of the cell. Does not stack with other cell spacing.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Right:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.spaceRight);
                spinner.setName("spacing-right");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The spacing to the right of the cell. Does not stack with other cell spacing.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Top:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.spaceTop);
                spinner.setName("spacing-top");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The spacing to the top of the cell. Does not stack with other cell spacing.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
    
                table.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                table.add(label);
    
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setValue(simCell.spaceBottom);
                spinner.setName("spacing-bottom");
                table.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The spacing to the bottom of the cell. Does not stack with other cell spacing.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private PopTable.PopTableClickListener cellSetWidgetListener() {
        var table = new Table();
        var scrollPane = new ScrollPane(table, skin, "scene");
        var scrollFocus = scrollPane;
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFlickScroll(false);
        scrollPane.addListener(main.getScrollFocusListener());
        
        
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
    
                var popTable = getPopTable();
                popTable.setWidth(popTable.getPrefWidth() + 50);
                popTable.validate();
                
                getStage().setScrollFocus(scrollFocus);
            }
        };
    
        var popTable = popTableClickListener.getPopTable();
        var label = new Label("Widgets:", skin, "scene-label-colored");
        popTable.add(label);
        label.addListener(new TextTooltip("Widgets are interactive components of your UI.", main.getTooltipManager(), skin, "scene"));
    
        label = new Label("Layout:", skin, "scene-label-colored");
        popTable.add(label);
        label.addListener(new TextTooltip("Layout widgets help organize the components of your UI and make it more adaptable to varying screen size.", main.getTooltipManager(), skin, "scene"));
    
        popTable.row();
        popTable.defaults().top();
        popTable.add(scrollPane).grow();
    
        var textButton = new TextButton("Button", skin, "scene-med");
        var valid = main.getJsonData().classHasValidStyles(Button.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Buttons are the most basic component to UI design. These are clickable widgets that can perform a certain action such as starting a game or activating a power.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.BUTTON, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("CheckBox", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(CheckBox.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("CheckBoxes are great for setting/displaying boolean values for an options screen.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.CHECK_BOX, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("Image", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Images are not directly interactable elements of a layout, but are necessary to showcase graphics or pictures in your UI. Scaling options make them a very powerful tool.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.IMAGE, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("ImageButton", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(ImageButton.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("A Button with an image graphic in it. The image can change depending on the state of the button.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.IMAGE_BUTTON, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("ImageTextButton", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(ImageTextButton.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("A Button with an image graphic followed by text in it. The image and text color can change depending on the state of the button.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.IMAGE_TEXT_BUTTON, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("Label", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(Label.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("The most common way to display text in your layouts. Wrapping and ellipses options help mitigate sizing issues in small spaces.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.LABEL, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("List", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(List.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("List presents text options in a clickable menu.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.LIST, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("ProgressBar", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(ProgressBar.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Commonly used to display loading progress or as a health/mana indicator in HUD's.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.PROGRESS_BAR, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("SelectBox", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(SelectBox.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("SelectBox is a kind of button that displays a selectable option list when opened.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.SELECT_BOX, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("Slider", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(Slider.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Slider is a kind of user interactable ProgressBar that allows a user to select a value along a sliding scale.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.SLIDER, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("TextButton", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(TextButton.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("A kind of button that contains a text element inside of it. The text color can change depending on the state of the button.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.TEXT_BUTTON, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("TextField", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(TextField.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("TextFields are the primary way of getting text input from the user.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.TEXT_FIELD, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("TextArea", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(TextField.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("TextAreas are a multiline version of a TextField.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.TEXT_AREA, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("Touchpad", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(Touchpad.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Touchpad is a UI element common to mobile games. It is used lieu of keyboard input, for example.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.TOUCH_PAD, popTable);
            }
        });
    
        table = new Table();
        scrollPane = new ScrollPane(table, skin, "scene");
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFlickScroll(false);
        popTable.add(scrollPane);
        scrollPane.addListener(main.getScrollFocusListener());
    
        table.row();
        textButton = new TextButton("Container", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Container is like a lightweight, single cell version of Table.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.CONTAINER, popTable);
            }
        });
        
        table.row();
        textButton = new TextButton("HorizontalGroup", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Allows layout of multiple elements horizontally. It is most useful for its wrap functionality, which cannot be achieved with a Table.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.HORIZONTAL_GROUP, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("ScrollPane", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Creates a scrollable layout for your widgets. It is commonly used to adapt the UI to variable content and screen sizes.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.SCROLL_PANE, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("Stack", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Allows stacking of elements on top of each other.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.STACK, popTable);
            }
        });
        
        table.row();
        textButton = new TextButton("SplitPane", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(SplitPane.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("An organizational layout that allows the user to adjust the width or height of two widgets next to each other.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.SPLIT_PANE, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("Table", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("The most powerful layout widget available. Consisting of a series of configurable cells, it organizes elements in rows and columns. It serves as the basis of all layout design in Scene2D.UI.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.TABLE, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("Tree", skin, "scene-med");
        valid = main.getJsonData().classHasValidStyles(Tree.class);
        textButton.setDisabled(!valid);
        table.add(textButton);
        if (valid) textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Tree is an organizational widget that allows collapsing and expanding elements like file structures.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.TREE, popTable);
            }
        });
    
        table.row();
        textButton = new TextButton("VerticalGroup", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Allows layout of multiple elements vertically. It is most useful for its wrap functionality, which cannot be achieved with a Table.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType.VERTICAL_GROUP, popTable);
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener checkBoxNameListener() {
        var simCheckBox = (DialogSceneComposerModel.SimCheckBox) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Name:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simCheckBox.name);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the CheckBox to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.checkBoxName(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener checkBoxStyleListener() {
        var simCheckBox = (DialogSceneComposerModel.SimCheckBox) simActor;
        var popTableClickListener = new StyleSelectorPopTable(CheckBox.class, simCheckBox.style == null ? "default" : simCheckBox.style.name) {
            @Override
            public void accepted(StyleData styleData) {
                events.checkBoxStyle(styleData);
            }
        };
        
        return popTableClickListener;
    }
    
    private EventListener checkBoxTextListener() {
        var simCheckBox = (DialogSceneComposerModel.SimCheckBox) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Text:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simCheckBox.text);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The text inside of the CheckBox.", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.checkBoxText(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener checkBoxCheckedListener() {
        var simCheckBox = (DialogSceneComposerModel.SimCheckBox) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Checked:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simCheckBox.checked ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simCheckBox.checked);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the CheckBox is checked initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.checkBoxChecked(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener checkBoxColorListener() {
        var simCheckBox = (DialogSceneComposerModel.SimCheckBox) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
    
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
        
                var label = new Label("Color:", skin, "scene-label-colored");
                popTable.add(label);
        
                popTable.row();
                var imageButton = new ImageButton(skin, "scene-color");
                imageButton.getImage().setColor(simCheckBox.color == null ? Color.WHITE : simCheckBox.color.color);
                popTable.add(imageButton).minWidth(100);
                imageButton.addListener(main.getHandListener());
                imageButton.addListener(new TextTooltip("Select the color of the CheckBox.", main.getTooltipManager(), skin, "scene"));
                imageButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogColors(new StyleProperty(), (colorData, pressedCancel) -> {
                            if (!pressedCancel) {
                                events.checkBoxColor(colorData);
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
    
                            }
    
                            @Override
                            public void closed() {
        
                            }
                        });
                    }
                });
        
                popTable.row();
                label = new Label(simCheckBox.color == null ? "white" : simCheckBox.color.getName(), skin, "scene-label-colored");
                popTable.add(label);
            }
        };
    
        popTableClickListener.update();
    
        return popTableClickListener;
    }
    
    private EventListener checkBoxPaddingListener() {
        var simCheckBox = (DialogSceneComposerModel.SimCheckBox) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var changeListener = new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Spinner padLeft = popTable.findActor("pad-left");
                        Spinner padRight = popTable.findActor("pad-right");
                        Spinner padTop = popTable.findActor("pad-top");
                        Spinner padBottom = popTable.findActor("pad-bottom");
                        events.checkBoxPadding((float) padLeft.getValue(), (float) padRight.getValue(), (float) padTop.getValue(), (float) padBottom.getValue());
                    }
                };
                
                var label = new Label("Padding:", skin, "scene-label-colored");
                popTable.add(label).colspan(2);
                
                popTable.row();
                popTable.defaults().right().spaceRight(5);
                label = new Label("Left:", skin, "scene-label-colored");
                popTable.add(label);
                
                var spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-left");
                spinner.setValue(simCheckBox.padLeft);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the left of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Right:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-right");
                spinner.setValue(simCheckBox.padRight);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the right of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Top:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-top");
                spinner.setValue(simCheckBox.padTop);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the top of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
                
                popTable.row();
                label = new Label("Bottom:", skin, "scene-label-colored");
                popTable.add(label);
                
                spinner = new Spinner(0, 1, true, Spinner.Orientation.RIGHT_STACK, skin, "scene");
                spinner.setName("pad-bottom");
                spinner.setValue(simCheckBox.padBottom);
                popTable.add(spinner);
                spinner.getTextField().addListener(main.getIbeamListener());
                spinner.getButtonMinus().addListener(main.getHandListener());
                spinner.getButtonPlus().addListener(main.getHandListener());
                spinner.addListener(new TextTooltip("The padding on the bottom of the contents.", main.getTooltipManager(), skin, "scene"));
                spinner.addListener(changeListener);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener checkBoxDisabledListener() {
        var simCheckBox = (DialogSceneComposerModel.SimCheckBox) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
    
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
        
                var label = new Label("Disabled:", skin, "scene-label-colored");
                popTable.add(label);
        
                popTable.row();
                var textButton = new TextButton(simCheckBox.disabled ? "TRUE" : "FALSE", skin, "scene-small");
                textButton.setChecked(simCheckBox.disabled);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the CheckBox is disabled initially.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.checkBoxDisabled(textButton.isChecked());
                    }
                });
            }
        };
    
        popTableClickListener.update();
    
        return popTableClickListener;
    }
    
    private EventListener checkBoxResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to reset this CheckBox?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets the settings of the TextButton to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.checkBoxReset();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener checkBoxDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to delete this CheckBox?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Removes this CheckBox from its parent.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.checkBoxDelete();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener imageNameListener() {
        var simImage = (DialogSceneComposerModel.SimImage) simActor;
        var textField = new TextField("", skin, "scene");
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Name:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simImage.name);
                popTable.add(textField).minWidth(150);
                textField.addListener(main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the Image to allow for convenient searching via Group#findActor().", main.getTooltipManager(), skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.imageName(textField.getText());
                    }
                });
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            popTable.hide();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                
                getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageDrawableListener() {
        var simImage = (DialogSceneComposerModel.SimImage) simActor;
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
    
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
        
                var label = new Label("Drawable:", skin, "scene-label-colored");
                popTable.add(label);
        
                popTable.row();
                var stack = new Stack();
                popTable.add(stack).minSize(100).maxSize(300).grow();
                var background = new Image(skin, "scene-tile-ten");
                stack.add(background);
                Image image;
                if (simImage.drawable != null) {
                    image = new Image(main.getAtlasData().drawablePairs.get(simImage.drawable));
                } else {
                    image = new Image((Drawable) null);
                }
                stack.add(image);
        
                popTable.row();
                var textButton = new TextButton("Select Drawable", skin, "scene-small");
                popTable.add(textButton).minWidth(100);
                textButton.addListener(main.getHandListener());
                textButton.addListener(new TextTooltip("The background drawable for the table.", main.getTooltipManager(), skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        popTable.hide();
                        main.getDialogFactory().showDialogDrawables(true, new DialogDrawables.DialogDrawablesListener() {
                            @Override
                            public void confirmed(DrawableData drawable, DialogDrawables dialog) {
                                events.imageDrawable(drawable);
                                image.setDrawable(main.getAtlasData().drawablePairs.get(drawable));
                            }
    
                            @Override
                            public void emptied(DialogDrawables dialog) {
                                events.imageDrawable(null);
                                image.setDrawable(null);
                            }
    
                            @Override
                            public void cancelled(DialogDrawables dialog) {
        
                            }
                        }, new DialogListener() {
                            @Override
                            public void opened() {
    
                            }
    
                            @Override
                            public void closed() {
        
                            }
                        });
                    }
                });
            }
        };
    
        popTableClickListener.update();
    
        return popTableClickListener;
    }
    
    private EventListener imageScalingListener() {
        var simImage = (DialogSceneComposerModel.SimImage) simActor;
        var selectBox = new SelectBox<Scaling>(skin, "scene");
        selectBox.setItems(Scaling.none, Scaling.fill, Scaling.fillX, Scaling.fillY, Scaling.fit, Scaling.stretch, Scaling.stretchX, Scaling.stretchY);
        var popTableClickListener = new PopTable.PopTableClickListener(skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Scaling:", skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                selectBox.setSelected(simImage.scaling);
                popTable.add(selectBox);
                selectBox.addListener(main.getHandListener());
                selectBox.addListener(new TextTooltip("The scaling strategy applied to the image.", main.getTooltipManager(), skin, "scene"));
                selectBox.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.imageScaling(selectBox.getSelected());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    private EventListener imageResetListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to reset this Image?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("RESET", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Resets the settings of the Image to their defaults.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.imageReset();
            }
        });
        
        return popTableClickListener;
    }
    
    private EventListener imageDeleteListener() {
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        
        var label = new Label("Are you sure you want to delete this Image?", skin, "scene-label-colored");
        popTable.add(label);
        
        popTable.row();
        var textButton = new TextButton("DELETE", skin, "scene-small");
        popTable.add(textButton).minWidth(100);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new TextTooltip("Removes this Image from its parent.", main.getTooltipManager(), skin, "scene"));
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                popTable.hide();
                events.imageDelete();
            }
        });
        
        return popTableClickListener;
    }
    
    
    
    public Dialog showConfirmCellSetWidgetDialog(DialogSceneComposerEvents.WidgetType widgetType, PopTable popTable) {
        var simCell = (DialogSceneComposerModel.SimCell) simActor;
        if (simCell.child == null) {
            popTable.hide();
            events.cellSetWidget(widgetType);
            return null;
        } else {
            var dialog = new Dialog("", skin, "scene-dialog") {
                @Override
                protected void result(Object object) {
                    if ((Boolean) object) {
                        popTable.hide();
                        events.cellSetWidget(widgetType);
                    }
                }
            };
    
            var root = dialog.getTitleTable();
            root.clear();
    
            root.add().uniform();
    
            var label = new Label("Confirm Overwrite Widget", skin, "scene-title");
            root.add(label).expandX();
    
            var button = new Button(skin, "scene-close");
            root.add(button).uniform();
            button.addListener(main.getHandListener());
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    dialog.hide();
                }
            });
    
            root = dialog.getContentTable();
            root.pad(10);
    
            label = new Label("This will overwrite the existing widget in the cell.\nAre you okay with that?", skin, "scene-label-colored");
            label.setWrap(true);
            label.setAlignment(Align.center);
            root.add(label).growX();
    
            dialog.getButtonTable().defaults().uniformX();
            var textButton = new TextButton("OK", skin, "scene-med");
            dialog.button(textButton, true);
            textButton.addListener(main.getHandListener());
    
            textButton = new TextButton("Cancel", skin, "scene-med");
            dialog.button(textButton, false);
            textButton.addListener(main.getHandListener());
            
            dialog.key(Input.Keys.ENTER, true).key(Input.Keys.SPACE, true);
            dialog.key(Input.Keys.ESCAPE, false);
    
            dialog.show(getStage());
            dialog.setSize(500, 200);
            dialog.setPosition((int) (getStage().getWidth() / 2f - dialog.getWidth() / 2f), (int) (getStage().getHeight() / 2f - dialog.getHeight() / 2f));
    
            return dialog;
        }
    }
    
    public void populatePath() {
        var root = pathTable;
        root.clear();
        
        var objects = new Array<DialogSceneComposerModel.SimActor>();
        objects.add(simActor);
        
        var object = simActor;
        
        while (object != null) {
            if (object.parent != null) {
                object = object.parent;
                objects.add(object);
            } else {
                object = null;
            }
        }
        
        while (objects.size > 0) {
            object = objects.pop();
            
            var textButton = new TextButton(object.toString(), skin, "scene-small");
            textButton.setUserObject(object);
            root.add(textButton);
            textButton.addListener(main.getHandListener());
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    simActor = (DialogSceneComposerModel.SimActor) textButton.getUserObject();
                    populateProperties();
                    populatePath();
                }
            });
            
            if (objects.size > 0) {
                var image = new Image(skin, "scene-icon-path-seperator");
                root.add(image);
            }
        }
    
        var popTableClickListener = new PopTable.PopTableClickListener(skin);
        var popTable = popTableClickListener.getPopTable();
        var popSubTable = new Table();
        var scrollPane = new ScrollPane(popSubTable, skin, "scene");
        popTable.add(scrollPane).grow();
        
        if (simActor instanceof DialogSceneComposerModel.SimTable) {
            var table = (DialogSceneComposerModel.SimTable) simActor;
            if (table.cells.size > 0) {
                var image = new Image(skin, "scene-icon-path-child-seperator");
                root.add(image);
    
                var textButton = new TextButton("Select Child", skin, "scene-small");
                root.add(textButton);
                textButton.addListener(main.getHandListener());
                
                int row = 0;
                for (var cell : table.cells) {
                    var textButton1 = new TextButton(cell.toString(), skin, "scene-small");
                    if (cell.row > row) {
                        popSubTable.row();
                        row++;
                    }
                    popSubTable.add(textButton1).colspan(cell.colSpan).fillX();
                    textButton1.addListener(main.getHandListener());
                    textButton1.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            simActor = cell;
                            populateProperties();
                            populatePath();
                            popTable.hide();
                        }
                    });
                }
                textButton.addListener(popTableClickListener);
            }
        } else if (simActor instanceof DialogSceneComposerModel.SimGroup) {
            var group = (DialogSceneComposerModel.SimGroup) simActor;
            if (group.children.size > 0) {
                var image = new Image(skin, "scene-icon-path-child-seperator");
                root.add(image);
    
                var textButton = new TextButton("Select Child", skin, "scene-small");
                root.add(textButton);
                textButton.addListener(main.getHandListener());
    
                for (var child : group.children) {
                    var textButton1 = new TextButton(child.toString(), skin, "scene-small");
                    popSubTable.add(textButton1).row();
                    textButton1.addListener(main.getHandListener());
                    textButton1.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            simActor = child;
                            populateProperties();
                            populatePath();
                            popTable.hide();
                        }
                    });
                }
                textButton.addListener(popTableClickListener);
            }
        } else if (simActor instanceof DialogSceneComposerModel.SimCell) {
            var cell = (DialogSceneComposerModel.SimCell) simActor;
            if (cell.child != null) {
                var image = new Image(skin, "scene-icon-path-child-seperator");
                root.add(image);
    
                var textButton = new TextButton("Select Child", skin, "scene-small");
                root.add(textButton);
                textButton.addListener(main.getHandListener());
    
                var textButton1 = new TextButton(cell.child.toString(), skin, "scene-small");
                popSubTable.add(textButton1);
                textButton1.addListener(main.getHandListener());
                textButton1.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        simActor = cell.child;
                        populateProperties();
                        populatePath();
                        popTable.hide();
                    }
                });
                textButton.addListener(popTableClickListener);
            }
        }
        
        root.add().growX();
    }
    
    @Override
    public Dialog show(Stage stage, Action action) {
        super.show(stage, action);
        stage.setScrollFocus(findActor("scroll-properties"));
        return this;
    }
    
    public Dialog showHelpDialog() {
        var dialog = new Dialog("", skin, "scene-dialog");
    
        var root = dialog.getTitleTable();
        root.clear();
    
        root.add().uniform();
    
        var label = new Label("About Scene Composer", skin, "scene-title");
        root.add(label).expandX();
    
        var button = new Button(skin, "scene-close");
        root.add(button).uniform();
        button.addListener(main.getHandListener());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });
    
        root = dialog.getContentTable();
        root.pad(10);
        
        label = new Label("Scene Composer is a live scenegraph editor for Scene2D.UI. The primary goal is to allow " +
                "Skin Composer users to test out their skins quickly in simple, reusable layouts. \n\nAs a consequence, " +
                "Scene Composer is not capable of making complex UI's. The user is encouraged to learn the nuances of " +
                "Scene2D and create their layouts via code. The export options are included as a convenience, not a " +
                "replacement for learning proper libGDX techniques.", skin, "scene-label-colored");
        label.setWrap(true);
        root.add(label).growX();
        
        var textButton = new TextButton("OK", skin, "scene-med");
        dialog.button(textButton);
        textButton.addListener(main.getHandListener());
        
        dialog.show(getStage());
        dialog.setSize(500, 350);
        dialog.setPosition((int) (getStage().getWidth() / 2f - dialog.getWidth() / 2f), (int) (getStage().getHeight() / 2f - dialog.getHeight() / 2f));
        
        return dialog;
    }
    
    public Dialog showImportDialog() {
        var dialog = new Dialog("", skin, "scene-dialog");
        
        var root = dialog.getTitleTable();
        root.clear();
        
        root.add().uniform();
        
        var label = new Label("Import", skin, "scene-title");
        root.add(label).expandX();
        
        var button = new Button(skin, "scene-close");
        root.add(button).uniform();
        button.addListener(main.getHandListener());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });
        
        root = dialog.getContentTable();
        root.pad(10);
        
        var table = new Table();
        root.add(table).growX();
        
        var textField = new TextField("", skin, "scene");
        table.add(textField).growX();
        textField.addListener(main.getIbeamListener());
        
        var textButton = new TextButton("Browse", skin, "scene-small");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                var file = main.getDesktopWorker().openDialog("Import Template...", main.getProjectData().getLastImportExportPath(), new String[] {"*.json"}, "JSON Files (*.json)");
            
                if (file != null) {
                    textField.setText(file.getPath());
                    textField.setCursorPosition(textField.getText().length());
                }
            }
        });
        
        dialog.getContentTable().row();
        table = new Table();
        root.add(table);
        
        table.defaults().space(5);
        textButton = new TextButton("Import Template", skin, "scene-med");
        table.add(textButton);
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.dialogImportImportTemplate(Gdx.files.absolute(textField.getText()));
                dialog.hide();
            }
        });
        
        label = new Label("Import JSON Template File", skin, "scene-label-colored");
        table.add(label).expandX().left();
        
        dialog.show(getStage());
        
        getStage().setKeyboardFocus(textField);
        
        return dialog;
    }
    
    public Dialog showExportDialog() {
        var dialog = new Dialog("", skin, "scene-dialog");
    
        var root = dialog.getTitleTable();
        root.clear();
    
        root.add().uniform();
    
        var label = new Label("Export", skin, "scene-title");
        root.add(label).expandX();
    
        var button = new Button(skin, "scene-close");
        root.add(button).uniform();
        button.addListener(main.getHandListener());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });
        
        root = dialog.getContentTable();
        root.pad(10);
        
        dialog.getContentTable().row();
        var table = new Table();
        root.add(table);
        
        table.defaults().space(5);
        var textButton = new TextButton("Save Template", skin, "scene-med");
        table.add(textButton).uniformX().fillX();
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                var file = main.getDesktopWorker().saveDialog("Export Template...", main.getProjectData().getLastImportExportPath(), new String[] {"*.json"}, "JSON Files (*.json)");
                events.dialogExportSaveTemplate(new FileHandle(file));
    
                if (file != null) {
                    dialog.hide();
                }
            }
        });
        
        label = new Label("A template to be imported into Scene Composer", skin, "scene-label-colored");
        table.add(label).expandX().left();
    
        table.row();
        textButton = new TextButton("Save to JAVA", skin, "scene-med");
        table.add(textButton).uniformX().fillX();
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                var file = main.getDesktopWorker().saveDialog("Export Java...", main.getProjectData().getLastImportExportPath(), new String[] {"*.java"}, "Java Files (*.java)");
                events.dialogExportSaveJava(new FileHandle(file));
                
                if (file != null) {
                    dialog.hide();
                }
            }
        });
    
        label = new Label("A file to be added directly into your project", skin, "scene-label-colored");
        table.add(label).expandX().left();
    
        table.row();
        textButton = new TextButton("Copy to Clipboard", skin, "scene-med");
        table.add(textButton).uniformX().fillX();
        textButton.addListener(main.getHandListener());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                events.dialogExportClipboard();
                dialog.hide();
            }
        });
    
        label = new Label("A minimal version to be pasted into your existing code", skin, "scene-label-colored");
        table.add(label).expandX().left();
        
        dialog.show(getStage());
        
        return dialog;
    }
    
    public Dialog showSettingsDialog() {
        var dialog = new Dialog("", skin, "scene-dialog");
        
        var root = dialog.getTitleTable();
        root.clear();
        
        root.add().uniform();
        
        var label = new Label("Settings", skin, "scene-title");
        root.add(label).expandX();
        
        var button = new Button(skin, "scene-close");
        root.add(button).uniform();
        button.addListener(main.getHandListener());
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });
        
        root = dialog.getContentTable();
        root.pad(10);
        
        var table = new Table();
        root.add(table).growX();
        
        table.defaults().left().space(5);
        label = new Label("Package", skin, "scene-label-colored");
        table.add(label);
        
        var textField = new TextField("", skin, "scene");
        var keyboardFocus = textField;
        table.add(textField).width(300).uniformX();
        textField.addListener(main.getIbeamListener());
    
        table.row();
        label = new Label("Class", skin, "scene-label-colored");
        table.add(label);
    
        textField = new TextField("", skin, "scene");
        table.add(textField).uniformX().fillX();
        textField.addListener(main.getIbeamListener());
    
        table.row();
        label = new Label("Skin Path", skin, "scene-label-colored");
        table.add(label);
    
        textField = new TextField("", skin, "scene");
        table.add(textField).uniformX().fillX();
        textField.addListener(main.getIbeamListener());
    
        table.row();
        label = new Label("Background Color", skin, "scene-label-colored");
        table.add(label);
    
        var imageButton = new ImageButton(new ImageButton.ImageButtonStyle(skin.get("scene-color", ImageButton.ImageButtonStyle.class)));
        table.add(imageButton).left();
        imageButton.addListener(main.getHandListener());
        
        dialog.getContentTable().row();
        table = new Table();
        root.add(table);
        
        dialog.show(getStage());
        
        getStage().setKeyboardFocus(keyboardFocus);
        
        return dialog;
    }
}