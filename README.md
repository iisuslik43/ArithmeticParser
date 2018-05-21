# ArithmeticParser

* To build application run:

`sudo chmod +x ./build.sh`

`./build.sh`

* How to run ArithmeticParser:

`./build/install/ArithmeticParser/bin/ArithmeticParser` - 
this is path to binary file, so
to run application use:

`./build/install/ArithmeticParser/bin/ArithmeticParser 
--file /path/to/file/filename`

To run application on test file run:

`./build/install/ArithmeticParser/bin/ArithmeticParser 
--file ./src/test/resources/testFile`

* How to run KekParser:

`./build/install/ArithmeticParser/bin/KekParser 
--file /path/to/file/filename`

* How to run KekTokens:

`./build/install/ArithmeticParser/bin/KekTokens
--file /path/to/file/filename`

# Синтаксис языка kek

Переносы строк и пробелы игнорируются после разбиения на токены, 
поэтому всё строится на скобочках и все пробелы 
оставлены, чтобы было читабельнее

* Модная грамматика для списка:

LIST(T) = `epsilon | T, LIST(T)`

* Операции

OP = `{+, -, *, /, %, ==, !=, >, >=, <, <=, &&, ||, =}`

* Числа
 
NUM - то, что принимается parseDouble() Котлина
 
* Идентификаторы языка

I = `[a-z_]\w*`

* Все выражения

ST = `IF | CALL ; | RW ; | WHILE | ASSIGN ; | return EXPR;`

* Маленькое выражение из чисел, идентификаторов и вызовов функций

EXPR = `V | V OP EXPR`

V = `NUM | I | CALL | FUN | true | false | (EXPR)`


* Функция, нет имени, потому что функции - это тоже объекты и их
нужно писать через =

FUN = `fun (LIST(I)) { ST* } `

* Создание переменной

ASSIGN = `I = EXPR`

* Вызов функции

CALL = `I(LIST(EXPR))`

* if else:

IF = `if (EXPR) {ST*} | if (EXPR) {ST*} else {ST*}`

* while:

WHILE = `while (EXPR) {ST*}`

* Чтение и запись

RW = `write(EXPR) | read(I)`

* Вся программа

Main = `List(ST)`

* Комментарии

Всё, что идёт после `//` на той же строке игнорируется

# Пример

```
function = fun (x, y) {
    if (x == y) {
        c = x + y;
        write(c);
    }
}

function2 = fun (f) {
    return f(2, 3);
}

x = 43;

while(x) {
    function2(function);
}

return 42;
```
# Синтаксический сахар

* Почти лямбды

`fun (LIST(I)) -> V` заменяется на `fun (LIST(I)) {return V;}`
(деревья у таких выражений будут одинаковыми)

Например:

`two = fun () -> 2` => `two = fun () { return 2}`

`f = fun (x) -> (fun() -> 23 + 22);` 

Заменяется на:

```
f = fun (x) { 
    return fun () {
            return 23; 
        } + 22;
    }
```

* Return IF

`return if (EXPR) { V1 } else { V2 };` заменяется на:

```
if(EXPR) {
    return V1
} else {
    return V2
}
```

* For loop

`for (ST1; EXPR; ST1) {List(ST)}` меняется на:

```
ST1;
while (EXPR) {
LIST(ST);
ST2;
}
```

Например:

```
for(i = 0; i < n; i = i + 1) {
    func(i);
    x = 43;
}
```

Заменится на:

```
i = 0;
while (i < n) {
    func(i);
    x = 43;
    i = i + 1;
}
```