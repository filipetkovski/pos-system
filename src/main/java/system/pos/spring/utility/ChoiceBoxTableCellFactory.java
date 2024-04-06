package system.pos.spring.utility;

import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;

public class ChoiceBoxTableCellFactory<T> {
    public static <T> TableCell<T, String> createCellFactory(ChoiceBox<String> choiceBox) {
        return new TableCell<T, String>() {
            {
                choiceBox.setOnAction(event -> {
                    if (isEditing())
                        commitEdit(choiceBox.getValue());
                });
            }

            @Override
            public void startEdit() {
                super.startEdit();
                setText(null);
                setGraphic(choiceBox);
                getTableView().edit(getIndex(), getTableColumn());
                Platform.runLater(() -> {
                    choiceBox.show();
                    choiceBox.requestFocus();
                });
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (isEditing() ? null : item));
                setGraphic(empty || !isEditing() ? null : choiceBox);
            }
        };
    }
}
