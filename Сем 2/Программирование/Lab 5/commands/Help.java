package commands;

public class Help extends Command {
    @Override
    public String getName() {
        return "HElP";
    }

    @Override
    public String getDesc() {
        return "Show information about commands.";
    }

    @Override
    public void execute(String arg) {
        System.out.println(
                """
        help : вывести справку по доступным командамw
        info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
        show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении
        add {element} : добавить новый элемент в коллекцию
        update id {element} : обновить значение элемента коллекции, id которого равен заданному
        remove_by_id id : удалить элемент из коллекции по его id
        clear : очистить коллекцию
        save : сохранить коллекцию в файл
        execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.
        exit : завершить программу (без сохранения в файл)
        head : вывести первый элемент коллекции
        remove_head : вывести первый элемент коллекции и удалить его
        remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный
        average_of_time_to_metro_by_transport : вывести среднее значение поля timeToMetroByTransport для всех элементов коллекции
        count_by_time_to_metro_on_foot timeToMetroOnFoot : вывести количество элементов, значение поля timeToMetroOnFoot которых равно заданному
        print_descending : вывести элементы коллекции в порядке убывания
                """
        );
    }

}
