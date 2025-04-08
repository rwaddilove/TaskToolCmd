// Task Tool - a text-based command line task/to-do manager
// By Roland Waddilove (github.com/rwaddilove/) as an exercise
// while learning Java. Public Domain. Use at your own risk!

// when to re-display tasks, eg. only after a change like done/remove

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

class Input {
    public static String InputStr(String prompt, int len) {
        System.out.print(prompt);
        Scanner input = new Scanner(System.in);
        String inp = input.nextLine().strip();
        return (inp.length() > len) ? inp.substring(0, len) : inp; }

    public static int InputInt(String prompt) {
        try {
            return Integer.parseInt(InputStr(prompt, 6)); }
        catch (NumberFormatException e) {
            return 9999; } }     // a value not used

    public static char InputChr(String prompt) {
        String inp = InputStr(prompt, 3).toLowerCase();
        return inp.isBlank() ? '*' : inp.charAt(0); }

    public static boolean isNumber(String s) {
        for (char c : s.toCharArray())
            if (!Character.isDigit(c)) return false;
        return true; }

    public static String InputDate(String prompt) {
        String inp = InputStr(prompt,15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        try {
            LocalDate date = LocalDate.parse(inp, formatter);}
        catch (DateTimeParseException dtpe) {
            System.out.println("Date not set (not recognised).");
            inp = ""; }
        return inp;
    }
}


class FileOp {

    public static void Read(String fp, ArrayList<ArrayList<String>> tasks) {
        tasks.clear();
        try (Scanner readFile = new Scanner(new File(fp))) {
            while (readFile.hasNextLine()) {
                String line = readFile.nextLine();
                line = line.substring(1, line.length()-1);        // strip first and last "
                String[] values = line.split("\",\"");      // split into task fields
                tasks.add(new ArrayList<>());
                for (String value : values)
                    tasks.getLast().add(value);
            }
        }
        catch (FileNotFoundException e) { System.out.println("'" + fp + "' not found."); }
    }

    public static void Write(String fp, ArrayList<ArrayList<String>> tasks) {
        try (FileWriter writeFile = new FileWriter(fp)) {
            for (ArrayList<String> tsk : tasks ) {
                String line = "\"";
                for (String s : tsk)
                    line += s + "\",\"";
                writeFile.write(line.substring(0, (line.length()-2)) + "\n");
            }
        }
        catch (IOException e) { System.out.println("Could not save " + fp); }
    }
}


class Task {

    public static void Show(ArrayList<ArrayList<String>> tasks) {
        int i = 0;
        System.out.println("\n   Title              Due        Repeat  Label      Done");
        for (ArrayList<String> tsk : tasks ) {
            String title = tsk.getFirst();
            if (title.length() > 16) title = title.substring(0,16) + "..";
            System.out.printf("%2d %-18s %-10s %-7s %-10s %-3s\n", i++, title, tsk.get(1), tsk.get(2), tsk.get(3), tsk.get(4)); }
        System.out.println();
    }

    public static String Remove(ArrayList<ArrayList<String>> tasks, int tsk) {
        if (tasks.isEmpty() || tsk < 0 || tsk >= tasks.size())
            return "Task not found. Use: TaskToolCmd remove <number>";
        tasks.remove(tsk);
        return "Task removed.";
    }

    public static String New(ArrayList<ArrayList<String>> tasks) {
        // title, due, repeat, label, done, notes
        String[] newtask = {"","","","","",""};
        System.out.println("\nADD NEW TASK:");
        newtask[0] = Input.InputStr("Task title: ", 30);
        if (newtask[0].isBlank()) return "Nothing added. Task must have a title!";
        newtask[1] = Input.InputDate("Due (yyyy-mm-dd): ");
        if (!newtask[1].isBlank()) {        // no date = no repeat
            newtask[2] = Input.InputStr("Repeat Day/Week/Month: ", 10);
            if (newtask[2].startsWith("d")) newtask[2] = "daily";
            if (newtask[2].startsWith("w")) newtask[2] = "weekly";
            if (newtask[2].startsWith("m")) newtask[2] = "monthly"; }
        newtask[3] = Input.InputStr("Label: ", 12);
        newtask[4] = "no";
        newtask[5] = Input.InputStr("Notes: ",200);
        tasks.add(new ArrayList<>());
        for (String tsk : newtask)
            tasks.getLast().add(tsk);
        return "Task added OK";
    }

    public static String Edit(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) return "Task not found. Use: TaskToolCmd edit <number>";
        String[] fields = {"Title", "Due", "Repeat", "Label", "Done", "Notes"};
        for (int i = 0; i < tasks.getFirst().size(); ++i)
            System.out.println(i + " " + fields[i] + ": " + tasks.get(task).get(i));
        int item = Input.InputInt("Edit which item? ");
        if (item < 0 || item >= tasks.getFirst().size())  return "Item not found.";

        // 0 title, 1 due, 2 repeat, 3 label, 4 done, 5 notes
        String inp;
        if (item == 0) {
            inp = Input.InputStr("Title: ", 30);
            if (inp.isBlank()) return "Not changed. Task must have a title.";
            tasks.get(task).set(item, inp); }
        if (item == 1) {
            inp = Input.InputDate("Due date (yyyy-mm-dd): ");
            tasks.get(task).set(item, inp); }
        if (item == 2) {
            inp = Input.InputStr("Repeat (D)aily, (W)eekly, (M)onthly: ", 10).toLowerCase();
            if (inp.startsWith("d")) tasks.get(task).set(item, "daily");
            else if (inp.startsWith("w")) tasks.get(task).set(item, "weekly");
            else if (inp.startsWith("m")) tasks.get(task).set(item, "monthly");
            else tasks.get(task).set(item, ""); }
        if (item == 3) {
            inp = Input.InputStr("Label: ", 12);
            tasks.get(task).set(item, inp); }
        if (item == 4) {
            inp = Input.InputStr("Is task done (y/n)? ", 3).toLowerCase();
            inp = (inp.startsWith("y")) ? "yes" : "no";
            tasks.get(task).set(item, inp); }
        if (item == 5) {
            inp = Input.InputStr("Notes: ", 200);
            tasks.get(task).set(item, inp); }
        return "Task updated";
    }

    public static void View(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) {
            System.out.println("Task not found. Use: TaskToolCmd view <number>");
            return; }
        String[] fields = {"Title", "Due", "Repeat", "Label", "Done", "Notes"};
        for (int i = 0; i < 6; ++i)
//            for (int i = 0; i < tasks.getFirst().size(); ++i)
            System.out.println(i + " " + fields[i] + ": " + tasks.get(task).get(i));
    }

    public static String Done(ArrayList<ArrayList<String>> tasks, int task) {
        if (tasks.isEmpty() || task < 0 || task >= tasks.size()) return "Task not found. Use: TaskToolCmd done <number>";
        tasks.get(task).set(4, "yes");      // task is done
        if (tasks.get(task).get(2).isBlank()) return "Task status updated";     // no repeat
        // set next due date for repeated tasks
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        LocalDate currentDate = LocalDate.now();
        LocalDate duedate = LocalDate.parse(tasks.get(task).get(1), formatter);
        do {    // skip missed past repeat dates, set next due date in future
            if (tasks.get(task).get(2).startsWith("d")) {
                duedate = duedate.plusDays(1);
            } else if (tasks.get(task).get(2).startsWith("w")) {
                duedate = duedate.plusWeeks(1);
            } else {
                duedate = duedate.plusMonths(1);
            }
        } while (currentDate.isAfter(duedate));
        tasks.get(task).set(1, duedate.toString());     // set next due date
        tasks.get(task).set(4, "no");                   // set not done
        return "Task updated for repeat task.\nNew due date set, not done set.";
    }

    public static void Overdue(ArrayList<ArrayList<String>> tasks) {
        // list tasks due today or overdue
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        LocalDate currentDate = LocalDate.now();
        String overdueTasks = "";
        String todayTasks = "";
        for (ArrayList<String> tsk : tasks) {
            if (tsk.get(1).isBlank() || tsk.get(4).equals("yes")) continue;  // no due date or done
            LocalDate tskdue = LocalDate.parse(tsk.get(1), formatter);
            if (currentDate.isAfter(tskdue))        // it's after due date?
                overdueTasks += String.format("Task: %-18s Due: %-10s\n", tsk.get(0), tsk.get(1));
            if (currentDate.equals(tskdue))         // due date is today?
                todayTasks += String.format("Task: %-18s Due: %-10s\n", tsk.get(0), tsk.get(1));
        }
        if (!todayTasks.isEmpty())
            System.out.println("You have tasks due today:\n" + todayTasks);
        if (!overdueTasks.isEmpty())
            System.out.println("You have tasks that are overdue!\n" + overdueTasks);
    }

    public static String Sort(ArrayList<ArrayList<String>> tasks, int index) {
        if (tasks.size() < 3) return "Not enough tasks to sort.";
        if (index != 0 && index != 1 && index != 3 && index != 4) return "Only sort on 0/1/3/4";
        for (int i = 0; i < tasks.size()-1; ++i)
            for (int j = tasks.size()-2; j >= i; --j)
                if (tasks.get(j).get(index).compareToIgnoreCase(tasks.get(j+1).get(index)) > 0)
                    Collections.swap(tasks, j, j+1);
        return "Sorted.";
    }
}


public class TaskToolCmd {
    public static void main(String[] args) {
        // title, due, repeat, label, done, notes
        ArrayList<ArrayList<String>> tasks = new ArrayList<>();
        File mac = new File("/users/shared");
        String filepath = mac.exists() ? "/users/shared/TaskTool.txt" : "/users/public/TaskTool.txt";
        FileOp.Read(filepath, tasks);
        System.out.println("----------------------------------------");
        System.out.println("           T A S K  T O O L");
        System.out.println("----------------------------------------");
        Task.Show(tasks);
        Task.Overdue(tasks);
        if (args.length > 0) {
            int item = 9999;        // an invalid task number
            if (Input.isNumber(args[1])) item = Integer.parseInt(args[1]);
            if (args[0].toLowerCase().startsWith("new")) System.out.println(Task.New(tasks));
            if (args[0].toLowerCase().startsWith("edit")) System.out.println(Task.Edit(tasks, item));
            if (args[0].toLowerCase().startsWith("done")) System.out.println(Task.Done(tasks, item));
            if (args[0].toLowerCase().startsWith("remove")) System.out.println(Task.Remove(tasks, item));
            if (args[0].toLowerCase().startsWith("view")) Task.View(tasks, item);
            if (args[0].toLowerCase().startsWith("sort")) System.out.println(Task.Sort(tasks, item));
            Task.Show(tasks);
        }
        FileOp.Write(filepath, tasks);
    }
}
