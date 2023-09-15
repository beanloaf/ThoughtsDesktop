package com.beanloaf.thoughtsdesktop.calendar.views.children.right_panel.children;

import com.beanloaf.thoughtsdesktop.calendar.enums.Weekday;
import com.beanloaf.thoughtsdesktop.calendar.objects.*;
import com.beanloaf.thoughtsdesktop.calendar.objects.schedule.ScheduleBoxItem;
import com.beanloaf.thoughtsdesktop.calendar.objects.schedule.ScheduleData;
import com.beanloaf.thoughtsdesktop.calendar.objects.schedule.ScheduleEvent;
import com.beanloaf.thoughtsdesktop.calendar.views.CalendarMain;
import com.beanloaf.thoughtsdesktop.calendar.views.children.right_panel.RightPanel;
import com.beanloaf.thoughtsdesktop.notes.changeListener.ThoughtsHelper;
import com.beanloaf.thoughtsdesktop.handlers.Logger;
import com.beanloaf.thoughtsdesktop.res.TC;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MonthView {

    private final RightPanel rightPanel;
    private final CalendarMain main;


    public final List<Runnable> queuedTasks = Collections.synchronizedList(new ArrayList<>());
    private DayEvent selectedEvent;

    private GridPane calendarFrame; // (7 x 5)


    /*  Left Panel  */
    private Button calendarEventsButton, calendarScheduleButton;
    private Node[] leftLayoutList;
    private Node calendarLeftEventPanel, calendarLeftSchedulePanel;


    /*  Event Box   */
    private VBox calendarEventBox;
    private Label calendarDayLabel;
    private Button calendarNewEventButton;


    /*  Schedule Box    */
    private Button calendarNewScheduleButton;
    public VBox calendarScheduleBox;


    /*  Small Event Input */
    private AnchorPane calendarSmallEventFields;
    private TextField calendarSmallEventTitleInput;
    private DatePicker calendarSmallDatePicker;
    private TimeGroupView calendarSmallTimeFrom, calendarSmallTimeTo;
    private TextArea calendarSmallEventDescriptionInput;
    private Button calendarSmallSaveEventButton, calendarSmallEditButton, calendarSmallDeleteButton;
    private HBox calendarSmallStartTimeFields, calendarSmallEndTimeFields;
    private Label calendarSmallFinalStartTimeLabel, calendarSmallFinalEndTimeLabel;
    private CheckBox calendarSmallProgressCheckBox;

    public MonthView(final RightPanel rightPanel) {
        this.rightPanel = rightPanel;
        this.main = rightPanel.getMain();

        locateNodes();
        attachEvents();


        changeMonth(this.main.getCalendarHandler().getCurrentMonth());
        swapLeftPanel(calendarLeftEventPanel);
        rightPanel.swapRightPanel(RightPanel.Layouts.MONTH);
    }

    private Node findNodeById(final String nodeId) {
        return rightPanel.findNodeById(nodeId);
    }

    private void locateNodes() {
        calendarFrame = (GridPane) findNodeById("calendarFrame");


        /*  Left Panel  */
        calendarEventsButton = (Button) findNodeById("calendarEventsButton");
        calendarScheduleButton = (Button) findNodeById("calendarScheduleButton");
        calendarLeftEventPanel = findNodeById("calendarLeftEventPanel");
        calendarLeftSchedulePanel = findNodeById("calendarLeftSchedulePanel");
        leftLayoutList = new Node[]{calendarLeftEventPanel, calendarLeftSchedulePanel};

        /*  Event Box   */
        calendarEventBox = (VBox) findNodeById("calendarEventBox");
        calendarDayLabel = (Label) findNodeById("calendarDayLabel");
        calendarNewEventButton = (Button) findNodeById("calendarNewEventButton");

        /*  Schedule Box    */
        calendarScheduleBox = (VBox) findNodeById("calendarScheduleBox");
        calendarNewScheduleButton = (Button) findNodeById("calendarNewScheduleButton");


        /*  Small Event Input   */
        calendarSmallEventFields = (AnchorPane) findNodeById("calendarSmallEventFields");
        calendarSmallEventTitleInput = (TextField) findNodeById("calendarSmallEventTitleInput");
        calendarSmallDatePicker = (DatePicker) findNodeById("calendarSmallDatePicker");


        final TextField calendarSmallHourInputFrom = (TextField) findNodeById("calendarSmallHourInputFrom");
        final TextField calendarSmallMinuteInputFrom = (TextField) findNodeById("calendarSmallMinuteInputFrom");
        final ComboBox<String> calendarSmallAMPMSelectorFrom = (ComboBox<String>) findNodeById("calendarSmallAMPMSelectorFrom");
        calendarSmallTimeFrom = new TimeGroupView(calendarSmallHourInputFrom, calendarSmallMinuteInputFrom, calendarSmallAMPMSelectorFrom);


        final TextField calendarSmallHourInputTo = (TextField) findNodeById("calendarSmallHourInputTo");
        final TextField calendarSmallMinuteInputTo = (TextField) findNodeById("calendarSmallMinuteInputTo");
        final ComboBox<String> calendarSmallAMPMSelectorTo = (ComboBox<String>) findNodeById("calendarSmallAMPMSelectorTo");
        calendarSmallTimeTo = new TimeGroupView(calendarSmallHourInputTo, calendarSmallMinuteInputTo, calendarSmallAMPMSelectorTo);


        calendarSmallEventDescriptionInput = (TextArea) findNodeById("calendarSmallEventDescriptionInput");
        calendarSmallSaveEventButton = (Button) findNodeById("calendarSmallSaveEventButton");
        calendarSmallEditButton = (Button) findNodeById("calendarSmallEditButton");
        calendarSmallDeleteButton = (Button) findNodeById("calendarSmallDeleteButton");


        calendarSmallEndTimeFields = (HBox) findNodeById("calendarSmallEndTimeFields");
        calendarSmallStartTimeFields = (HBox) findNodeById("calendarSmallStartTimeFields");

        calendarSmallFinalStartTimeLabel = (Label) findNodeById("calendarSmallFinalStartTimeLabel");
        calendarSmallFinalEndTimeLabel = (Label) findNodeById("calendarSmallFinalEndTimeLabel");

        calendarSmallProgressCheckBox = (CheckBox) findNodeById("calendarSmallProgressCheckBox");

    }

    public void startupMonthView() {
        this.main.getCalendarHandler().getDay(LocalDate.now()).onClick();
    }

    public void onOpen() {
        calendarFrame.requestFocus();
    }

    private void attachEvents() {
        calendarNewScheduleButton.setOnAction(e -> this.main.swapOverlay(CalendarMain.Overlays.SCHEDULE, new ScheduleData()));

        /*  Left Panel  */
        calendarEventsButton.setOnAction(e -> swapLeftPanel(calendarLeftEventPanel));
        calendarScheduleButton.setOnAction(e -> swapLeftPanel(calendarLeftSchedulePanel));


        /*  Small New Event*/
        calendarSmallEventFields.setVisible(false);

        calendarSmallProgressCheckBox.selectedProperty().addListener((observableValue, aBoolean, isChecked) ->
                calendarSmallProgressCheckBox.setText(isChecked ? "Completed" : "In-progress"));

        calendarSmallProgressCheckBox.setOnAction(e -> selectedEvent.setCompleted(calendarSmallProgressCheckBox.isSelected(), true));


        calendarNewEventButton.setOnMouseClicked(e -> {
            final CalendarDay selectedDay = this.main.getCalendarHandler().getSelectedDay();
            if (selectedDay != null)
                selectEvent(addNewEventToCalendarDay(selectedDay.getYear(), selectedDay.getMonth(), selectedDay.getDay()), true);
        });

        calendarSmallSaveEventButton.setVisible(false);
        calendarSmallSaveEventButton.setOnAction(e -> {
            if (selectedEvent == null) return;
            saveEvent(selectedEvent);


            selectDay(this.main.getCalendarHandler().getSelectedDay());
            selectEvent(selectedEvent, false);
        });

        calendarSmallDeleteButton.setOnAction(e -> {
            if (selectedEvent == null) return;
            deleteEvent(selectedEvent);
        });


        calendarSmallEditButton.setOnAction(e -> {
            toggleSmallEventFields(true);
            calendarSmallSaveEventButton.setVisible(true);
        });


        Platform.runLater(() -> calendarScheduleBox.getChildren().clear());
    }


    private void swapLeftPanel(final Node pane) {
        for (final Node anchorPane : leftLayoutList) {
            anchorPane.setVisible(false);
        }

        if (pane != null) pane.setVisible(true);

    }


    public void changeMonth(final CalendarMonth month) {
        this.main.getCalendarHandler().removeInactiveMonths();
        this.main.getCalendarHandler().setCurrentMonth(month.getMonth(), month.getYear());
        createCalendarGUI();
    }


    private void createCalendarGUI() {
        final CalendarMonth currentMonth = this.main.getCalendarHandler().getCurrentMonth();

        rightPanel.setHeaderText(ThoughtsHelper.toCamelCase(currentMonth.getMonth().toString()) + ", " + currentMonth.getYear());
        final int monthLength = currentMonth.getMonthLength();

        final CalendarMonth prevMonth = this.main.getCalendarHandler().getMonth(currentMonth.getPreviousMonth().getMonth(), currentMonth.getPreviousMonth().getYear());
        final CalendarMonth nextMonth = this.main.getCalendarHandler().getMonth(currentMonth.getNextMonth().getMonth(), currentMonth.getNextMonth().getYear());

        Platform.runLater(() -> {
            calendarFrame.getChildren().clear();

            int row = 0;
            int col = 0;
            int day = 0;

            int overflowDays = 1;

            int prevDays = currentMonth.getPreviousMonth().getMonthLength() - (currentMonth.getStartingDayOfWeek() - 1);

            for (int i = 0; i < calendarFrame.getColumnCount() * calendarFrame.getRowCount(); i++) {

                if ((row == 0 && col < currentMonth.getStartingDayOfWeek())) { // first row, before the first day of the month
                    CalendarDay calendarDay = prevMonth.getDay(prevDays);
                    if (calendarDay == null) {
                        calendarDay = new CalendarDay(prevMonth.getYear(), prevMonth.getMonth(), prevDays, main);
                    }
                    calendarDay.checkIsToday();
                    prevDays++;
                    calendarFrame.add(calendarDay, col, row);


                } else if (day >= monthLength) { // after the last day of the month
                    CalendarDay calendarDay = nextMonth.getDay(overflowDays);
                    if (calendarDay == null) {
                        calendarDay = new CalendarDay(nextMonth.getYear(), nextMonth.getMonth(), overflowDays, main);
                    }
                    calendarDay.checkIsToday();

                    overflowDays++;
                    calendarFrame.add(calendarDay, col, row);


                } else { // normal month
                    day++;

                    CalendarDay calendarDay = currentMonth.getDay(day);
                    if (calendarDay == null) {
                        calendarDay = new CalendarDay(currentMonth.getYear(), currentMonth.getMonth(), day, main);
                        currentMonth.addDay(day, calendarDay);
                    }
                    calendarDay.checkIsToday();
                    calendarFrame.add(calendarDay, col, row);
                }


                col++;

                if (col % 7 == 0) {
                    row++;
                    col = 0;
                }

            }

            synchronized (queuedTasks) {
                for (final Runnable runnable : queuedTasks) {
                    runnable.run();
                }
                queuedTasks.clear();
            }
        });
    }

    public void hideSchedule(final ScheduleData data, final boolean isHidden) {
        if (isHidden) {
            if (data.getStartDate() != null && data.getEndDate() != null) {
                final long daysBetween = ChronoUnit.DAYS.between(data.getStartDate(), data.getEndDate()) + 1;

                LocalDate date = data.getStartDate();
                for (int i = 0; i < daysBetween; i++) {
                    final CalendarDay day = this.main.getCalendarHandler().getDay(LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth()));
                    for (final DayEvent event : day.getEvents()) {
                        if (data.getId() != null && data.getId().equals(event.getEventID())) {
                            Platform.runLater(() -> day.removeEvent(event));
                        }

                    }
                    date = date.plusDays(1);
                }
            }
        } else {
            addScheduleToCalendarDay(data);
        }


    }


    public void updateSchedule(final ScheduleData data, final LocalDate oldStartDate, final LocalDate oldEndDate) {
        if (oldStartDate != null && oldEndDate != null) {
            final long daysBetween = ChronoUnit.DAYS.between(oldStartDate, oldEndDate) + 1;

            LocalDate date = oldStartDate;
            for (int i = 0; i < daysBetween; i++) {
                final CalendarDay day = this.main.getCalendarHandler().getDay(LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth()));
                for (final DayEvent event : day.getEvents()) {
                    if (data.getId() != null && data.getId().equals(event.getEventID())) {
                        Platform.runLater(() -> day.removeEvent(event));
                    }

                }
                date = date.plusDays(1);

            }
        }


        boolean boxExists = false;
        for (final Node node : calendarScheduleBox.getChildrenUnmodifiable()) {
            if (node.getClass() != ScheduleBoxItem.class) continue;
            final ScheduleBoxItem scheduleBoxItem = (ScheduleBoxItem) node;
            if (scheduleBoxItem.getScheduleId().equals(data.getId())) {
                boxExists = true;
                scheduleBoxItem.setHidden(false);
                break;
            }

        }

        if (!boxExists)
            Platform.runLater(() -> calendarScheduleBox.getChildren().add(new ScheduleBoxItem(this.main, data)));


        addScheduleToCalendarDay(data);

    }


    public void deleteSchedule(final ScheduleBoxItem scheduleBoxItem) {
        calendarScheduleBox.getChildren().remove(scheduleBoxItem);
        new File(TC.Directories.CALENDAR_SCHEDULES_PATH, scheduleBoxItem.getScheduleId() + ".json").delete();

    }

    public void addScheduleToCalendarDay(final ScheduleData schedule) {
        LocalDate startDate = schedule.getStartDate();
        final LocalDate endDate = schedule.getEndDate();


        if (startDate == null || endDate == null) {
            Logger.log("Needs to have a startdate and enddate. This needs to be changed to not have this issue.");
            return;
        }


        final long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;


        for (int i = 0; i < daysBetween; i++) {
            for (final ScheduleEvent scheduleEvent : schedule.getScheduleEventList()) {
                boolean isCorrectDay = false;

                for (final Weekday weekday : scheduleEvent.getWeekdays()) {
                    if (weekday.getDayOfWeek() == startDate.getDayOfWeek().getValue() || (weekday.getDayOfWeek() == 0 && startDate.getDayOfWeek().getValue() == 7)) {
                        isCorrectDay = true;
                        break;
                    }
                }

                if (!isCorrectDay) continue;


                final DayEvent dayEvent = new DayEvent(startDate, scheduleEvent.getScheduleEventName(), schedule.getId(), main, true);
                dayEvent.setDescription(scheduleEvent.getDescription());
                dayEvent.setStartTime(scheduleEvent.getStartTime());
                dayEvent.setEndTime(scheduleEvent.getEndTime());

                LocalDate finalStartDate = startDate;
                Platform.runLater(() -> addEventToCalendarDay(finalStartDate, dayEvent));

            }
            startDate = startDate.plusDays(1);
        }


    }


    public DayEvent addEventToCalendarDay(final LocalDate date, final DayEvent event) {
        final Month month = date.getMonth();
        final int year = date.getYear();
        final int day = date.getDayOfMonth();

        final CalendarMonth activeMonth = this.main.getCalendarHandler().getMonth(month, year);

        if (day > activeMonth.getMonthLength()) throw new IllegalArgumentException("Day out of bounds. " + day);

        activeMonth.getDay(day).addEvent(event);

        return event;
    }

    private DayEvent addNewEventToCalendarDay(final int year, final Month month, final int day) {
        final CalendarMonth activeMonth = this.main.getCalendarHandler().getMonth(month, year);


        if (day < 0 || day > activeMonth.getMonthLength())
            throw new IllegalArgumentException("Day out of bounds. " + day);

        final CalendarDay calendarDay = activeMonth.getDay(day);


        final DayEvent event = new DayEvent(LocalDate.of(calendarDay.getYear(), calendarDay.getMonth(), calendarDay.getDay()), "New Event", main, false);
        event.setDescription("");
        event.setStartTime(null);

        this.main.getJsonHandler().addEventToJson(event);
        calendarDay.addEvent(event);

        return event;

    }


    public void selectDay(final CalendarDay day) {
        if (day == this.main.getCalendarHandler().getSelectedDay()) return;

        this.main.getCalendarHandler().setSelectedDay(day);

        Platform.runLater(() -> {
            calendarDayLabel.setText(ThoughtsHelper.toCamelCase(day.getMonth().toString()) + " " + day.getDay()
                    + ThoughtsHelper.getNumberSuffix(day.getDay()) + ", " + day.getYear());

            calendarSmallEventFields.setVisible(false);
            calendarEventBox.getChildren().clear();

            for (final DayEvent dayEvent : day.getEvents()) {
                final DayEvent clone = new DayEvent(dayEvent, main);
                calendarEventBox.getChildren().add(clone);
            }
        });


    }

    public void selectDay(final LocalDate date) {
        final CalendarMonth month = this.main.getCalendarHandler().getMonth(date.getMonth(), date.getYear());
        selectDay(month.getDay(date.getDayOfMonth()));

    }


    public void selectEvent(DayEvent event, final boolean editable) {

        swapLeftPanel(calendarLeftEventPanel);

        event.getStyleClass().add("selected-day-event");
        if (selectedEvent != null) selectedEvent.getStyleClass().remove("selected-day-event");
        selectedEvent = event;

        calendarSmallEventFields.setVisible(true);

        toggleSmallEventFields(editable);
        calendarSmallSaveEventButton.setVisible(editable);
        calendarSmallEditButton.setVisible(!editable);


        calendarSmallEventTitleInput.setText(event.getEventTitle());
        calendarSmallDatePicker.setValue(LocalDate.of(event.getDate().getYear(), event.getDate().getMonth(), event.getDate().getDayOfMonth()));

        calendarSmallProgressCheckBox.setSelected(event.isCompleted());

        final LocalTime startTime = event.getStartTime();
        final LocalTime endTime = event.getEndTime();

        calendarSmallTimeFrom.setTime(startTime);
        calendarSmallTimeTo.setTime(endTime);

        calendarSmallFinalStartTimeLabel.setText(startTime == null ? "" : "@ " + startTime.format(DateTimeFormatter.ofPattern("h:mm a")));
        calendarSmallFinalEndTimeLabel.setText(endTime == null ? "" : "till " + endTime.format(DateTimeFormatter.ofPattern("h:mm a")));

        calendarSmallEventDescriptionInput.setText(event.getDescription());
    }

    private void toggleSmallEventFields(final boolean isEnabled) {
        final boolean isDisabled = !isEnabled;
        calendarSmallEventTitleInput.setDisable(isDisabled);
        calendarSmallDatePicker.setDisable(isDisabled);


        final ObservableList<String> styles = calendarSmallDatePicker.getStyleClass();
        final String disableDatePickerStyle = "non-editable-date-picker";


        if (isDisabled) {
            if (!styles.contains(disableDatePickerStyle)) styles.add(disableDatePickerStyle);
        } else {
            while (styles.contains(disableDatePickerStyle)) styles.remove(disableDatePickerStyle);
        }

        calendarSmallStartTimeFields.setVisible(isEnabled);
        calendarSmallEndTimeFields.setVisible(isEnabled);

        calendarSmallFinalStartTimeLabel.setVisible(isDisabled);
        calendarSmallFinalEndTimeLabel.setVisible(isDisabled);

        calendarSmallEventDescriptionInput.setDisable(isDisabled);

    }

    public void saveEvent(DayEvent event) {
        event.setEventTitle(calendarSmallEventTitleInput.getText());

        final LocalDate oldDate = event.getDate();
        if (oldDate != null && !calendarSmallDatePicker.getValue().isEqual(oldDate)) {
            deleteEvent(event, oldDate);
            event.setStartDate(calendarSmallDatePicker.getValue());
            selectEvent(addEventToCalendarDay(event.getDate(), event), false);
            selectDay(event.getDate());
        } else {
            event.setStartDate(calendarSmallDatePicker.getValue());

        }

        event.setStartTime(calendarSmallTimeFrom.getTime());
        if (calendarSmallTimeFrom.getTime() != null) {
            try {
                event.setEndTime(calendarSmallTimeFrom.getTime().isBefore(calendarSmallTimeTo.getTime()) ? calendarSmallTimeTo.getTime() : null);
            } catch (Exception e) {
                event.setEndTime(null);
            }
        } else {
            event.setEndTime(calendarSmallTimeTo.getTime());
        }


        event.setDescription(calendarSmallEventDescriptionInput.getText());

        final LocalTime startTime = event.getStartTime();
        final LocalTime endTime = event.getStartTime();
        calendarSmallFinalStartTimeLabel.setText(startTime == null ? "" : "@ " + startTime.format(DateTimeFormatter.ofPattern("h:mm a")));
        calendarSmallFinalEndTimeLabel.setText(endTime == null ? "" : "till " + endTime.format(DateTimeFormatter.ofPattern("h:mm a")));

        calendarSmallSaveEventButton.setVisible(false);
        toggleSmallEventFields(false);

        this.main.getJsonHandler().addEventToJson(event);
        rightPanel.getWeekView().refreshWeek();


    }

    private void deleteEvent(final DayEvent event) {
        deleteEvent(event, event.getDate());
    }

    private void deleteEvent(final DayEvent event, final LocalDate oldDate) {
        this.main.getJsonHandler().removeEventFromJson(selectedEvent, oldDate);

        final Month month = event.getDate().getMonth();
        final int year = event.getDate().getYear();
        final int day = event.getDate().getDayOfMonth();

        final CalendarMonth calendarMonth = this.main.getCalendarHandler().getMonth(month, year);

        calendarMonth.getDay(day).removeEvent(event);
        selectDay(this.main.getCalendarHandler().getSelectedDay());

        if (rightPanel.getCurrentLayout() == RightPanel.Layouts.WEEK) {
            rightPanel.getWeekView().refreshWeek();
        }


    }


}