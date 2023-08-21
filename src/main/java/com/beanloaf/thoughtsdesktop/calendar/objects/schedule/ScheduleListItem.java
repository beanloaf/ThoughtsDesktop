package com.beanloaf.thoughtsdesktop.calendar.objects.schedule;

import com.beanloaf.thoughtsdesktop.calendar.objects.CH;
import com.beanloaf.thoughtsdesktop.calendar.objects.Weekday;
import com.beanloaf.thoughtsdesktop.calendar.views.SchedulePopup;
import com.beanloaf.thoughtsdesktop.handlers.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScheduleListItem extends GridPane {



    private final SchedulePopup view;


    private final Map<Weekday, CheckBox> checkBoxMap = new HashMap<>();

    private final List<Weekday> weekdays = new ArrayList<>();

    private final ScheduleEvent event;

    private final Label displayText;

    private final List<ScheduleLabel> references = new ArrayList<>();



    public ScheduleListItem(final SchedulePopup view, final String scheduleName) {
        this(view, scheduleName, UUID.randomUUID().toString());

    }

    public ScheduleListItem(final SchedulePopup view, final String scheduleName, final String id) {
        this(view, new ScheduleEvent(scheduleName, id));

    }


    public ScheduleListItem(final SchedulePopup view, final ScheduleEvent event) {
        super();
        this.view = view;
        this.event = event;

        this.getStyleClass().add("schedule");


        displayText = new Label(event.getScheduleEventName());
        displayText.setStyle("-fx-font-family: Lato; -fx-font-size: 18;");
        this.add(displayText, 0, 0);

        final ColumnConstraints weekendColumnConstraints = new ColumnConstraints();
        weekendColumnConstraints.setPrefWidth(100);
        weekendColumnConstraints.setMinWidth(10);
        weekendColumnConstraints.setHgrow(Priority.SOMETIMES);

        final GridPane weekdayPane = new GridPane();
        for (int i = 0; i < Weekday.values().length; i++) {
            final Weekday weekday = Weekday.values()[i];


            final Label eventLabel = new Label(weekday.getAbbreviation());
            eventLabel.setAlignment(Pos.CENTER);
            eventLabel.maxWidthProperty().setValue(10000);
            weekdayPane.add(eventLabel, i, 0);


            final CheckBox checkBox = new CheckBox();
            checkBox.setAlignment(Pos.CENTER);
            checkBox.maxWidthProperty().setValue(10000);

            checkBox.selectedProperty().addListener((observableValue, aBoolean, isChecked) -> {

                if (isChecked) {
                    view.addScheduleEventToDay(weekday, this);

                    if (!weekdays.contains(weekday)) weekdays.add(weekday);
                }
                else {
                    view.removeScheduleFromDay(weekday, this);

                    while (weekdays.contains(weekday)) weekdays.remove(weekday);
                }

            });


            checkBoxMap.put(weekday, checkBox);

            weekdayPane.add(checkBox, i, 1);
            weekdayPane.getColumnConstraints().add(weekendColumnConstraints);
        }
        this.add(weekdayPane, 1, 0);

        final RowConstraints firstRow = new RowConstraints();
        firstRow.percentHeightProperty().setValue(40);
        weekdayPane.getRowConstraints().add(firstRow);

        final RowConstraints secondRow = new RowConstraints();
        secondRow.percentHeightProperty().setValue(60);
        weekdayPane.getRowConstraints().add(secondRow);


        final ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(50);
        columnConstraints.setMinWidth(10);
        columnConstraints.setHgrow(Priority.SOMETIMES);

        getColumnConstraints().add(columnConstraints);
        getColumnConstraints().add(columnConstraints);


        this.setOnMouseClicked(e -> doClick());

    }




    public void doClick() {
        view.setInputFields(this);

    }

    public ScheduleEvent getEvent() {
        return this.event;
    }



    public void setScheduleName(final String newName) {
        event.setScheduleEventName(newName);
        displayText.setText(newName);

        for (final ScheduleLabel scheduleLabel : references) {
            scheduleLabel.setText(newName);

        }
    }

    public String getScheduleName() {
        return event.getScheduleEventName();
    }

    public void setDescription(final String newDescription) {
        event.setDescription(newDescription);
    }

    public String getDescription() {
        return event.getDescription();
    }


    public void setStartTime(final LocalTime startTime) {
        this.event.setStartTime(startTime);
    }


    public void setStartTime(final String hourString, final String minuteString, final String period) {
        setStartTime(CH.validateStringIntoTime(hourString, minuteString, period));
    }



    public void setEndTime(final LocalTime endTime) {
        this.event.setEndTime(endTime);
    }


    public void setEndTime(final String hourString, final String minuteString, final String period) {
        setEndTime(CH.validateStringIntoTime(hourString, minuteString, period));
    }



    public LocalTime getStartTime() {
        return event.getStartTime();
    }

    public LocalTime getEndTime() {
        return event.getEndTime();
    }


    public String getDisplayTime(final LocalTime time) {
        String formattedTime = "";
        if (time != null) {
            formattedTime = time.format(DateTimeFormatter.ofPattern("h:mm a")) + " | ";
            if (formattedTime.contains("AM")) {
                formattedTime = formattedTime.replace(" AM", "a");
            } else {
                formattedTime = formattedTime.replace(" PM", "p");

            }
        }
        return formattedTime;
    }

    public void addReference(final ScheduleLabel event) {
        this.references.add(event);
    }

    public void removeReference(final ScheduleLabel event) {
        this.references.remove(event);
    }


    public ScheduleLabel getLabel() {
        return new ScheduleLabel(this);
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ScheduleListItem scheduleListItem = (ScheduleListItem) o;
        return event.equals(scheduleListItem.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event);
    }


    @Override
    public String toString() {
        return "ScheduleListItem {" +
                "weekdays=" + weekdays +
                ", event=" + event +
                ", displayText=" + displayText +
                '}';
    }

    public static class ScheduleLabel extends Label {

        private final ScheduleListItem scheduleListItem;

        public ScheduleLabel(final ScheduleListItem scheduleListItem) {
            super(scheduleListItem.getScheduleName());

            this.scheduleListItem = scheduleListItem;


            this.getStyleClass().add("day-event");
            this.setMaxWidth(Double.MAX_VALUE);

            final Tooltip tooltip = new Tooltip(scheduleListItem.getScheduleName());
            tooltip.setShowDelay(Duration.seconds(0.5));
            this.setTooltip(tooltip);

            this.setOnMouseClicked(e -> {
                Logger.log("Schedule \"" + this.scheduleListItem.getScheduleName() + "\" was pressed.");
                scheduleListItem.doClick();
            });

        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            final ScheduleLabel that = (ScheduleLabel) other;
            return Objects.equals(scheduleListItem, that.scheduleListItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scheduleListItem);
        }
    }



}
