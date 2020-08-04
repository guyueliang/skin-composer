package com.ray3k.skincomposer.dialog.scenecomposer.menulisteners;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.ray3k.stripe.DraggableTextList;
import com.ray3k.stripe.PopTableClickListener;
import com.ray3k.stripe.Spinner;
import com.ray3k.skincomposer.data.StyleData;
import com.ray3k.skincomposer.dialog.scenecomposer.DialogSceneComposer;
import com.ray3k.skincomposer.dialog.scenecomposer.DialogSceneComposerEvents;
import com.ray3k.skincomposer.dialog.scenecomposer.DialogSceneComposerModel;
import com.ray3k.skincomposer.dialog.scenecomposer.DialogSceneComposerModel.SimActor;
import com.ray3k.skincomposer.dialog.scenecomposer.StyleSelectorPopTable;

import static com.ray3k.skincomposer.dialog.scenecomposer.menulisteners.ListenersUtils.TEXT_FIELD_WIDTH;

public class SelectBoxListeners {
    public static EventListener selectBoxNameListener(final DialogSceneComposer dialogSceneComposer) {
        var simSelectBox = (DialogSceneComposerModel.SimSelectBox) dialogSceneComposer.simActor;
        var textField = new TextField("", DialogSceneComposer.skin, "scene");
        var popTableClickListener = new PopTableClickListener(DialogSceneComposer.skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                dialogSceneComposer.getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
                
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Name:", DialogSceneComposer.skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                textField.setText(simSelectBox.name);
                popTable.add(textField).minWidth(TEXT_FIELD_WIDTH);
                textField.addListener(DialogSceneComposer.main.getIbeamListener());
                textField.addListener(new TextTooltip("The name of the SelectBox to allow for convenient searching via Group#findActor().", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                textField.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        dialogSceneComposer.events.selectBoxName(textField.getText());
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
                
                dialogSceneComposer.getStage().setKeyboardFocus(textField);
                textField.setSelection(0, textField.getText().length());
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    public static EventListener selectBoxStyleListener(final DialogSceneComposerEvents events, SimActor simActor) {
        var simSelectBox = (DialogSceneComposerModel.SimSelectBox) simActor;
        var popTableClickListener = new StyleSelectorPopTable(SelectBox.class, simSelectBox.style == null ? "default" : simSelectBox.style.name) {
            @Override
            public void accepted(StyleData styleData) {
                events.selectBoxStyle(styleData);
            }
        };
        
        return popTableClickListener;
    }
    
    public static EventListener selectBoxTextListListener(final DialogSceneComposer dialogSceneComposer) {
        var simSelectBox = (DialogSceneComposerModel.SimSelectBox) dialogSceneComposer.simActor;
        var textField = new TextField("", DialogSceneComposer.skin, "scene");
        textField.setFocusTraversal(false);
        var draggableTextList = new DraggableTextList(true, DialogSceneComposer.skin, "scene");
        draggableTextList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialogSceneComposer.events.selectBoxSelected(draggableTextList.getSelectedIndex());
                dialogSceneComposer.events.selectBoxList(draggableTextList.getTexts());
            }
        });
        var scrollPane = new ScrollPane(draggableTextList, DialogSceneComposer.skin, "scene");
        scrollPane.setFadeScrollBars(false);
        scrollPane.addListener(DialogSceneComposer.main.getScrollFocusListener());
        var popTableClickListener = new PopTableClickListener(DialogSceneComposer.skin) {
            {
                popTable.setAutomaticallyResized(true);
                
                textField.addListener(DialogSceneComposer.main.getIbeamListener());
                textField.addListener(new TextTooltip("The text to add to the list.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                textField.addListener(new InputListener() {
                    @Override
                    public boolean keyDown(InputEvent event, int keycode) {
                        if (keycode == Input.Keys.ENTER) {
                            addItem(textField.getText());
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            }
            
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("List Entries:\nDrag to change order\nClick the default option", DialogSceneComposer.skin, "scene-label-colored");
                label.setAlignment(Align.center);
                popTable.add(label).colspan(2);
                
                popTable.row();
                draggableTextList.clearChildren();
                draggableTextList.addAllTexts(simSelectBox.list);
                if (draggableTextList.getTexts().size > 0) {
                    draggableTextList.setSelected(simSelectBox.selected);
                }
                popTable.add(scrollPane).colspan(2).minHeight(150).growX();
                
                popTable.row();
                label = new Label("Add New Item:", DialogSceneComposer.skin, "scene-label-colored");
                popTable.add(label).colspan(2).padTop(10);
                
                popTable.row();
                textField.setText("");
                popTable.add(textField).minWidth(TEXT_FIELD_WIDTH);
                
                var textButton = new Button(DialogSceneComposer.skin, "scene-plus");
                popTable.add(textButton);
                textButton.addListener(DialogSceneComposer.main.getHandListener());
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        addItem(textField.getText());
                    }
                });
                
                dialogSceneComposer.getStage().setKeyboardFocus(textField);
                dialogSceneComposer.getStage().setScrollFocus(scrollPane);
            }
            
            public void addItem(String item) {
                draggableTextList.addText(item);
                dialogSceneComposer.events.selectBoxList(draggableTextList.getTexts());
                update();
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    public static EventListener selectBoxMaxListCountListener(final DialogSceneComposerEvents events,
                                                              SimActor simActor) {
        var simSelectBox = (DialogSceneComposerModel.SimSelectBox) simActor;
        var popTableClickListener = new PopTableClickListener(DialogSceneComposer.skin) {
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
                
                var label = new Label("Max List Count:", DialogSceneComposer.skin, "scene-label-colored");
                table.add(label).right();
                
                var valueSpinner = new Spinner(simSelectBox.maxListCount, 1, true, Spinner.Orientation.RIGHT_STACK, DialogSceneComposer.skin, "scene");
                valueSpinner.setMinimum(0);
                table.add(valueSpinner).width(100).left();
                valueSpinner.getTextField().addListener(DialogSceneComposer.main.getIbeamListener());
                valueSpinner.getButtonMinus().addListener(DialogSceneComposer.main.getHandListener());
                valueSpinner.getButtonPlus().addListener(DialogSceneComposer.main.getHandListener());
                valueSpinner.addListener(new TextTooltip("The maximum visible entries in the list.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                valueSpinner.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxMaxListCount(valueSpinner.getValueAsInt());
                    }
                });
                
                table.row();
                label = new Label("(Set to 0 to show as many as possible)", DialogSceneComposer.skin, "scene-label-colored");
                table.add(label).colspan(2);
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    public static EventListener selectBoxAlignmentListener(final DialogSceneComposerEvents events,
                                                           final SimActor simActor) {
        var popTableClickListener = new PopTableClickListener(DialogSceneComposer.skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var simSelectBox = (DialogSceneComposerModel.SimSelectBox) simActor;
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var table = new Table();
                popTable.add(table);
                
                var label = new Label("Alignment:", DialogSceneComposer.skin, "scene-label-colored");
                table.add(label).colspan(3);

                table.row();
                table.defaults().space(10).left().uniformX();
                var buttonGroup = new ButtonGroup<ImageTextButton>();
                var imageTextButton = new ImageTextButton("Top-Left", DialogSceneComposer.skin, "scene-checkbox-colored");
                var topLeft = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the top left.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.topLeft);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                imageTextButton = new ImageTextButton("Top", DialogSceneComposer.skin, "scene-checkbox-colored");
                var top = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the top center.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.top);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                imageTextButton = new ImageTextButton("Top-Right", DialogSceneComposer.skin, "scene-checkbox-colored");
                var topRight = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the top right.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.topRight);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                table.row();
                imageTextButton = new ImageTextButton("Left", DialogSceneComposer.skin, "scene-checkbox-colored");
                var left = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the middle left.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.left);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                imageTextButton = new ImageTextButton("Center", DialogSceneComposer.skin, "scene-checkbox-colored");
                var center = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the center.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.center);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                imageTextButton = new ImageTextButton("Right", DialogSceneComposer.skin, "scene-checkbox-colored");
                var right = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the middle right.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.right);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                table.row();
                imageTextButton = new ImageTextButton("Bottom-Left", DialogSceneComposer.skin, "scene-checkbox-colored");
                var bottomLeft = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the bottom left.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.bottomLeft);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                imageTextButton = new ImageTextButton("Bottom", DialogSceneComposer.skin, "scene-checkbox-colored");
                var bottom = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the bottom center.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.bottom);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                imageTextButton = new ImageTextButton("Bottom-Right", DialogSceneComposer.skin, "scene-checkbox-colored");
                var bottomRight = imageTextButton;
                imageTextButton.setProgrammaticChangeEvents(false);
                table.add(imageTextButton);
                imageTextButton.addListener(DialogSceneComposer.main.getHandListener());
                imageTextButton.addListener(new TextTooltip("Align the contents of the SelectBox to the bottom right.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                imageTextButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        events.selectBoxAlignment(Align.bottomRight);
                    }
                });
                buttonGroup.add(imageTextButton);
                
                switch (simSelectBox.alignment) {
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
    
    public static EventListener selectBoxScrollingListener(final DialogSceneComposerEvents events, SimActor simActor) {
        var simSelectBox = (DialogSceneComposerModel.SimSelectBox) simActor;
        var popTableClickListener = new PopTableClickListener(DialogSceneComposer.skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Scrolling Disabled:", DialogSceneComposer.skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simSelectBox.scrollingDisabled ? "TRUE" : "FALSE", DialogSceneComposer.skin, "scene-small");
                textButton.setChecked(simSelectBox.scrollingDisabled);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(DialogSceneComposer.main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the SelectBox scrolling is diabled", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.selectBoxScrollingDisabled(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
    
    public static EventListener selectBoxDisabledListener(final DialogSceneComposerEvents events, SimActor simActor) {
        var simSelectBox = (DialogSceneComposerModel.SimSelectBox) simActor;
        var popTableClickListener = new PopTableClickListener(DialogSceneComposer.skin) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                update();
            }
            
            public void update() {
                var popTable = getPopTable();
                popTable.clearChildren();
                
                var label = new Label("Disabled:", DialogSceneComposer.skin, "scene-label-colored");
                popTable.add(label);
                
                popTable.row();
                var textButton = new TextButton(simSelectBox.disabled ? "TRUE" : "FALSE", DialogSceneComposer.skin, "scene-small");
                textButton.setChecked(simSelectBox.disabled);
                popTable.add(textButton).minWidth(100);
                textButton.addListener(DialogSceneComposer.main.getHandListener());
                textButton.addListener(new TextTooltip("Whether the SelectBox is disabled initially.", DialogSceneComposer.main.getTooltipManager(), DialogSceneComposer.skin, "scene"));
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        textButton.setText(textButton.isChecked() ? "TRUE" : "FALSE");
                        events.selectBoxDisabled(textButton.isChecked());
                    }
                });
            }
        };
        
        popTableClickListener.update();
        
        return popTableClickListener;
    }
}
