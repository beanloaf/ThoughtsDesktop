package com.beanloaf.thoughtsdesktop.objects.calendar;

import com.beanloaf.thoughtsdesktop.changeListener.ThoughtsHelper;
import com.beanloaf.thoughtsdesktop.handlers.Logger;
import com.beanloaf.thoughtsdesktop.views.CalendarView;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class CalendarDay extends AnchorPane {

    final CalendarView view;

    private final LocalDate date;

    private final VBox eventContainer;

    private final List<DayEvent> eventList = new ArrayList<>();


    public CalendarDay(final Integer year, final Month month, final Integer day, final CalendarView view) {
        super();
        this.view = view;


        date = day == null ? null : LocalDate.of(year, month, day);


        ThoughtsHelper.setAnchor(this, 0.0, 0.0, 0.0, 0.0);
        this.getStyleClass().add("calendar-day");


        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("calendar-day");
        scrollPane.getStyleClass().add("edge-to-edge");
        scrollPane.fitToWidthProperty().set(true);

        scrollPane.skinProperty().addListener((observableValue, skin, t1) -> {
            StackPane stackPane = (StackPane) scrollPane.lookup("ScrollPane .viewport");
            stackPane.setCache(false);
        });
        this.getChildren().add(ThoughtsHelper.setAnchor(scrollPane, 0.0, 0.0, 0.0, 0.0));


        eventContainer = new VBox();
        eventContainer.getStyleClass().add("events");
        eventContainer.setMinHeight(0);


        // Triggers when a day event is clicked
        this.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                onClick();
                final Node node = (Node) e.getTarget();

                if (node.getId() != null && node.getId().equals(DayEvent.ID)) return;

            }


        });

        scrollPane.setContent(eventContainer);



        final Label dateLabel = new Label(day != null ? Integer.toString(day) : "");
        this.getChildren().add(ThoughtsHelper.setAnchor(dateLabel, 4.0, null, null, 10.0));


    }

    public void onClick() {
        view.populateEventBox(this);

    }

    public Integer getDay() {
        return this.date.getDayOfMonth();
    }

    public Month getMonth() {
        return this.date.getMonth();
    }

    public Integer getYear() {
        return this.date.getYear();
    }

    public DayEvent addEvent(final String eventName) {
        final DayEvent eventLabel = new DayEvent(this, eventName, view);
        eventList.add(eventLabel);

        eventContainer.getChildren().add(eventLabel);

        return eventLabel;

    }

    public void removeEvent(final String eventName) {


    }

    public DayEvent[] getEvents() {
        return eventList.toArray(new DayEvent[0]);
    }



}