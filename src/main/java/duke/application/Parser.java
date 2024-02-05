package duke.application;
import duke.io.Ui;
import duke.io.Storage;
import duke.task.Deadlines;
import duke.task.Event;
import duke.task.Todo;
import duke.task.Task;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


/**
 * The Parser class is responsible for parsing user input and executing corresponding commands.
 * It handles various commands such as listing tasks, marking/unmarking tasks, adding todos,
 * deadlines, and events, removing tasks, and exiting the application.
 */
public class Parser {

    /**
     * Parses the user input into an array of strings, separating the command and the remaining input.
     *
     * @param input The user input to be parsed.
     * @return An array containing the command and the remaining input.
     */
    public static String[] parseInput(String input) {
        return input.split(" ", 2);
    }

    /**
     * Handles the execution of commands based on the parsed user input.
     *
     * @param input    The user input to be processed.
     * @param taskList The task list to be manipulated.
     */
    public void handleCommand(String input, TaskList taskList) {
        String[] parsedInput = Parser.parseInput(input);

        if ("list".equalsIgnoreCase(input)) {
            Ui.showTaskList(taskList.getTasks());

        } else if ("bye".equalsIgnoreCase(input)) {
            Ui.showByeMessage();
            Storage.saveTasks(taskList.getTasks());
            System.exit(0);
        } else if (parsedInput[0].equalsIgnoreCase("mark") || parsedInput[0].equalsIgnoreCase("unmark")) {
            markingHandler(input, taskList);
        } else if (parsedInput[0].equalsIgnoreCase("deadline")) {
            if (validateDeadlineInput(input)) {
                handleDeadlines(input, taskList);
            } else {
                Ui.showErrorMessage("Please complete your request by specifying the details of the task!");
            }
        } else if (parsedInput[0].equalsIgnoreCase("todo")) {
            if (validateTodoInput(input)) {
                handleTodos(input, taskList);
            } else {
                Ui.showErrorMessage("Please complete your request by specifying the details of the task!");
            }
        } else if (parsedInput[0].equalsIgnoreCase("event")) {
            if (validateEventInput(input)) {
                handleEvents(input, taskList);
            } else {
                Ui.showErrorMessage("Please complete your request by specifying the details of the task!");
            }
        } else if (parsedInput[0].equalsIgnoreCase("delete")) {
            handleRemove(input, taskList);
        } else {
            Ui.showErrorMessage("I'm sorry, I don't understand! Please type your request again.");
        }
    }

    /**
     * Handles the marking or unmarking of tasks based on user input.
     *
     * @param input    The user input specifying the task number and the action.
     * @param taskList The task list to be manipulated.
     */
    public static void markingHandler(String input, TaskList taskList) {
        String[] split = input.split(" ");

        if (split.length < 2) {
            Ui.showErrorMessage("Please specify the task number!");
            return;
        }

        try {
            int index = Integer.parseInt(split[1]) - 1;
            Task task = taskList.getTasks().get(index);

            if ("mark".equalsIgnoreCase(split[0])) {
                task.markAsDone();
                Storage.saveTasks(taskList.getTasks());
                Ui.showMarkedAsDone(task);
                Ui.printLine();
            } else if ("unmark".equalsIgnoreCase(split[0])) {
                task.unmarkTask();
                Storage.saveTasks(taskList.getTasks());
                Ui.showUnmarkedTask(task);
                Ui.printLine();
            }

        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            Ui.showErrorMessage("Invalid task number. Please refer to your to-do list again.");
        }
    }

    /**
     * Handles the creation and addition of Todo tasks to the task list.
     *
     * @param input    User input specifying the Todo description.
     * @param taskList The task list to which the Todo task will be added.
     */
    private static void handleTodos(String input, TaskList taskList) {
        String description = input.substring(5).trim();
        Todo todo = new Todo(description);
        taskList.addTask(todo);
        Storage.saveTasks(taskList.getTasks());
        System.out.println("Ok! I've added this todo: " + todo);
        System.out.println("Now you have " + taskList.getTotalTasks() + " tasks in your list.");
        Ui.printLine();
    }

    /**
     * Handles the creation and addition of Deadline tasks to the task list.
     *
     * @param input    User input specifying the Deadline description and due date.
     * @param taskList The task list to which the Deadline task will be added.
     */
    private static void handleDeadlines(String input, TaskList taskList) {
        String[] splitParts = input.substring(9).split("/by", 2);

        if (splitParts.length > 1) {
            String description = splitParts[0].trim();
            String date = splitParts[1].trim();
            if (isValidDate(date)) {
                LocalDate d1 = LocalDate.parse(date, DateTimeFormatter.ofPattern("M/d/yyyy HHmm"));
                Deadlines deadline = new Deadlines(description, d1);
                taskList.addTask(deadline);
                Storage.saveTasks(taskList.getTasks());
                System.out.println("Ok! I've added this deadline: " + deadline);

            } else {
                Deadlines deadline = new Deadlines(description, date);
                taskList.addTask(deadline);
                Storage.saveTasks(taskList.getTasks());
                System.out.println("Ok! I've added this deadline: " + deadline);
            }
            System.out.println("Now you have " + taskList.getTotalTasks() + " tasks in your list.");
            Ui.printLine();
        }
        else {
            System.out.println("Invalid input format for deadline. Please provide a valid date/time.");
        }
    }

    /**
     * Handles the creation and addition of Event tasks to the task list.
     *
     * @param input    User input specifying the Event description and date range.
     * @param taskList The task list to which the Event task will be added.
     */
    private static void handleEvents(String input, TaskList taskList) {
        String[] splitParts = input.substring(6).split("/from", 2);
        String[] splitTo = splitParts[1].split("/to", 2);

        if (splitTo.length > 1) {
            String description = splitParts[0].trim();
            String fromDate = splitTo[0].trim();
            String toDate = splitTo[1].trim();
            if (isValidDate(fromDate) && isValidDate(toDate)) {
                LocalDate d1 = LocalDate.parse(fromDate);
                LocalDate d2 = LocalDate.parse(toDate);
                Event event = new Event(description, d1, d2);
                taskList.addTask(event);
                Storage.saveTasks(taskList.getTasks());
                Ui.showTaskAdded(event, taskList.getTotalTasks());
                Ui.printLine();
            } else {
                Ui.showErrorMessage("Invalid input format for event. Please provide valid dates.");
            }
        } else {
            Ui.showErrorMessage("Invalid input format for event. Please provide valid date/time.");
        }
    }

    /**
     * Handles the removal of tasks from the task list based on user input.
     *
     * @param input    User input specifying the task number to be removed.
     * @param taskList The task list from which the task will be removed.
     */
    private static void handleRemove(String input, TaskList taskList) {
        String[] splitParts = input.split(" ");
        if (splitParts.length < 2) {
            System.out.println("Please specify which task number you want to remove!");
            return;
        }
        try {
            int index = Integer.parseInt(splitParts[1]) - 1;
            Task removedTask = taskList.removeTask(index);
            Storage.saveTasks(taskList.getTasks());
            Ui.showTaskRemoved(removedTask, taskList.getTotalTasks());
            Ui.printLine();
        } catch (IndexOutOfBoundsException e) {
            Ui.showErrorMessage("Invalid task number. Please refer to your to-do list again.");
        }
    }

    /**
     * Validates the input format for creating Deadlines.
     * @param input User input specifying the Deadline description and due date.
     * @return True if the input format is valid, false otherwise.
     */
    private static boolean validateDeadlineInput(String input) {
        String[] splitParts = input.substring(9).split("/by", 2);
        return splitParts.length > 1;
    }

    /**
     * Validates the input format for creating Todo tasks.
     * @param input User input specifying the Todo description.
     * @return True if the input format is valid, false otherwise.
     */
    private static boolean validateTodoInput(String input) {
        return input.length() > 5;
    }

    /**
     * Validates the input format for creating Events.
     * @param input User input specifying the Event description and date range.
     * @return True if the input format is valid, false otherwise.
     */
    private static boolean validateEventInput(String input) {
        String[] splitParts = input.substring(6).split("/from", 2);
        return splitParts.length > 1;
    }

    /**
     * Validates the input format for a date string.
     *
     * @param input The date string to be validated.
     * @return True if the input is a valid date string, false otherwise.
     */
    private static boolean isValidDate(String input) {
        try {
            LocalDate.parse(input, DateTimeFormatter.ofPattern("M/d/yyyy HHmm"));
            return true;
        } catch (DateTimeParseException e1) {
            try {
                LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return true;
            } catch (DateTimeParseException e2) {
                return false;
            }

        }
    }

}

