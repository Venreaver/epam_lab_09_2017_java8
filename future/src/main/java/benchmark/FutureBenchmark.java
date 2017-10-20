package benchmark;

import data.raw.Employee;
import data.raw.Generator;
import data.raw.JobHistoryEntry;
import data.typed.Employer;
import data.typed.TypedJobHistoryEntry;
import data.typed.Position;
import data.typed.TypedEmployee;
import db.SlowBlockingDb;
import db.SlowCompletableFutureDb;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@State(Scope.Benchmark)
public class FutureBenchmark {

    @Param({"400", "4000", "40000"})
    public int requestCount;

    @Param({"10000"})
    public int employeesCount;

    private SlowBlockingDb<Employer> blockingEmployers; // база работодателей
    private SlowBlockingDb<Position> blockingPositions; // база позиций
    private SlowBlockingDb<Employee> blockingEmployee;  // база сотрудников
    private ExecutorService blockingExecutorService;    // пулл
    private List<String> requests;                      // запросы

    @Setup
    public void setup() {
        blockingEmployers = createDbForEnum(Employer.values()); // создаем базу работодателей
        blockingPositions = createDbForEnum(Position.values()); // создаем базу позиций
//?!
        // создаем Map работников: не совсем понятно, как тут работает merge в toMap
        Map<String, Employee> employeeMap = Generator.generateEmployeeList(employeesCount)
                                                     .stream()
//?!
                                                      // merge: старое значение возвращается?
                                                     .collect(toMap(Employee::toString, Function.identity(), (e1, e2) -> e1));
        blockingEmployee = new SlowBlockingDb<>(employeeMap);   // создаем базу работников
        // вынимаем массив ключей из employeeMap -> Employee::toString
        String[] keys = employeeMap.keySet().toArray(new String[0]);
        // генерируем список запросов: по сути перемешиваем ключи?
        requests = Stream.generate(() -> keys[ThreadLocalRandom.current().nextInt(keys.length)])
                         .limit(requestCount * 10)
                         .distinct()
                         .limit(requestCount)
                         .collect(toList());
        // создаем пул потоков
        blockingExecutorService = Executors.newCachedThreadPool();
    }

    // создание БД в виде SlowBlockingDb, там где-то внутри зарыта Map
    private static <T extends Enum<T>> SlowBlockingDb<T> createDbForEnum(T[] values) {
        return new SlowBlockingDb<>(Arrays.stream(values).collect(toMap(T::name, Function.identity())));
    }

    // закрытие всего, что мы насоздавали
    @TearDown
    public void tearDown() throws Exception {
        blockingExecutorService.shutdownNow();
        blockingExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        blockingPositions.close();
        blockingEmployers.close();
        blockingEmployee.close();
    }

    @Benchmark
    public void blockingProcessing(Blackhole bh) {
        // получаем список Future
        List<Future<?>> futures = requests.stream()
                                          .map(requestToFuture(bh, this::blockingGetTypedEmployee))
                                          .collect(toList());
        // в цикле получаем от всех Future get
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }
    }

    // функция, которая переводит из String-ключа в TypedEmployee
    private TypedEmployee blockingGetTypedEmployee(String key) {
        try {
            // по ключу вынимаем Employee
            Employee employee = blockingEmployee.get(key);
            // создаем список TypedJobHistoryEntry для сотрудника
            List<TypedJobHistoryEntry> entries = employee.getJobHistory()
                                                         .stream()
                                                          // типизируем JobHistoryEntry для всех
                                                         .map(this::typifyJobHistoryEntry)
                                                         .collect(toList());
            // возвращаем типизированного Employee, т.е. Employee со списком TypedJobHistoryEntry
            return new TypedEmployee(employee.getPerson(), entries);
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    // типизируем JobHistoryEntry
    private TypedJobHistoryEntry typifyJobHistoryEntry(JobHistoryEntry entry) {
        try {
            // создаем TypedJobHistoryEntry на основе значений, полученных по ключу из БД Позиций и Работодателей + Duration
            return new TypedJobHistoryEntry(blockingPositions.get(entry.getPosition()),
                                            blockingEmployers.get(entry.getEmployer()),
                                            entry.getDuration());
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

//?!
    // уточнить еще раз, зачем нам нужна Blackhole
    // принимает Blackhole, функцию, которая переводит из String в TypedEmployee, возвращает функцию, которая переводит из String в Future
    private Function<String, Future<?>> requestToFuture(Blackhole blackhole, Function<String, TypedEmployee> executorRequest) {
        return request -> blockingExecutorService.submit(() -> blackhole.consume(executorRequest.apply(request)));
    }

    @Benchmark
    public void futureProcessing(Blackhole bh) {
        // получаем список Future
        List<Future<?>> futures = requests.stream()
                                          .map(requestToFuture(bh, this::futureGetTypedEmployee))
                                          .collect(toList());
        // в цикле получаем от всех Future get
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    // функция, которая переводит из String-ключа в TypedEmployee
    private TypedEmployee futureGetTypedEmployee(String key) {
        try {
            // по ключу вынимаем Employee
            Employee employee = blockingEmployee.get(key);
            // созддаем Map<String, Future> с типизированым Future для работодателей и позиций
            Map<String, Future<Employer>> employers = new HashMap<>();
            Map<String, Future<Position>> positions = new HashMap<>();
//?!
            // пробегаемся по getJobHistory Employee и для каждой записи вносим данные в вышесозданные Map
            // Мы заранее подготовили Future для работодателей и позиций с помощью обращения к БД, которая возвращает Future
            // Это ли позволяет сократить время обработки запросов в сравнении с blockingProcessing?
            for (JobHistoryEntry entry : employee.getJobHistory()) {
                employers.put(entry.getEmployer(), blockingEmployers.getFutureDb().get(entry.getEmployer()));
                positions.put(entry.getPosition(), blockingPositions.getFutureDb().get(entry.getPosition()));
            }
            // создаем список TypedJobHistoryEntry для сотрудника
            List<TypedJobHistoryEntry> jobHistoryEntries = employee.getJobHistory()
                                                                   .stream()
                                                                    // здесь уже с помощью заготовленных Map с Future типизируем все JobHistoryEntry
                                                                   .map(entry -> new TypedJobHistoryEntry(
                                                                                        getOrNull(positions.get(entry.getPosition())),
                                                                                        getOrNull(employers.get(entry.getEmployer())),
                                                                                        entry.getDuration()))
                                                                   .collect(toList());
            // возвращаем типизированного Employee, т.е. Employee со списком TypedJobHistoryEntry
            return new TypedEmployee(employee.getPerson(), jobHistoryEntries);
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Benchmark
    public void completableProcessing(Blackhole bh) {
        // получаем список Future
        List<Future<?>> futures = requests.stream()
                                          .map(request -> completableFutureGetTypedEmployee(request).thenAccept(bh::consume))
                                          .collect(toList());
        // в цикле получаем от всех Future get
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // функция, которая переводит из String-ключа в TypedEmployee
    private CompletableFuture<TypedEmployee> completableFutureGetTypedEmployee(String key) {
        // создаем немного другую базу сотрудников, такую, которая будет уже SlowCompletableFutureDb,
        // т.е. достаем то, что внутри blockingEmployee.getFutureDb()
        SlowCompletableFutureDb<Employee> employeeDb = blockingEmployee.getFutureDb().getCompletableFutureDb();
        // достаем из базы сотрудника
        CompletableFuture<Employee> employee = employeeDb.get(key);
        // просим CompletableFuture сделать thenCompose
        // по сути thenCompose это тоже самое, что и thenApply
        // обработка происходит с помощью asyncToTyped(Employee employee)
        return employee.thenCompose(this::asyncToTyped);
    }

    // функция трансформирует Employee в CompletionStage<TypedEmployee>
    private CompletionStage<TypedEmployee> asyncToTyped(Employee employee) {
        // создаем список TypedJobHistoryEntry для сотрудника, применяя asyncToTyped(JobHistoryEntry entry)
        List<CompletableFuture<TypedJobHistoryEntry>> jobHistoryFutures = employee.getJobHistory()
                                                                                  .stream()
                                                                                  .map(this::asyncToTyped)
                                                                                  .collect(toList());
//?!
        // allOf возвращает результат комбинации всех CompletableFuture, которые были в него переданы:
        // мы туда кладем массив CompletableFuture, который получается из jobHistoryFutures
        // дальше мы делаем thenApply, то есть применяем функцию, которая из каждого CompletableFuture<TypedJobHistoryEntry>
        // собирает список типизированных JobHistoryEntry
        // затем этот список используется для создания типизированного Employee
        return CompletableFuture.allOf(jobHistoryFutures.toArray(new CompletableFuture[0]))
                                .thenApply(x -> {
                                    List<TypedJobHistoryEntry> jobHistory = jobHistoryFutures.stream()
                                                                                             // здесь мы вытаскиваем из CompletableFuture<TypedJobHistoryEntry>
                                                                                             // TypedJobHistoryEntry или null
                                                                                             .map(FutureBenchmark::getOrNull)
                                                                                             .collect(toList());

                                    return new TypedEmployee(employee.getPerson(), jobHistory);
                                });
    }

    // функция трансформирует JobHistoryEntry в CompletableFuture<TypedJobHistoryEntry>
    private CompletableFuture<TypedJobHistoryEntry> asyncToTyped(JobHistoryEntry entry) {
        // создаем БД для работодателей и позиций, такие БД достаем из xxx.getFutureDb()
        // возвращать эти ДБ будут CompletableFuture
        SlowCompletableFutureDb<Employer> employersDb = blockingEmployers.getFutureDb().getCompletableFutureDb();
        SlowCompletableFutureDb<Position> positionDb = blockingPositions.getFutureDb().getCompletableFutureDb();
        // получаем из employersDb по ключу работодателя
        // затем результат комбинируем с тем, что получаем из positionDb по ключу позиции
        // результатом комбинации выступает TypedJobHistoryEntry(работодатель, позиция, duration из изначального entry)
        return employersDb.get(entry.getEmployer())
                          .thenCombine(positionDb.get(entry.getPosition()), (e, p) -> new TypedJobHistoryEntry(p, e, entry.getDuration()));
    }

    private static <T> T getOrNull(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
            return null;
        }
    }
}
