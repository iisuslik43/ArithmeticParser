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

* How to run LParser:

`./build/install/ArithmeticParser/bin/LParser 
--file /path/to/file/filename`

#Синтаксис L

Переносы строк и пробелы игнорируются после разбиения на токены, 
поэтому всё строится на скобочках и все пробелы 
оставлены, чтобы было читабельнее

* Модная грамматика для списка:

LIST(T) = `epsilon | T, LIST(T)`

* Операции

OP = `{+, -, *, /, %, ==, !=, >, >=, <, <=, &&, ||, =}`

* Числа
 
NUM =
 
* Идентификаторы языка

I = `[a-z_]\w*`

* Все выражения

ST = `IF | EXPR ; | Write ; | WHILE | ASSIGN ; | return EXPR;`

* Маленькое выражение из чисел, идентификаторов и вызовов функций

EXPR = `V | V OP EXPR`

V = `NUM | I | CALL | FUN | read | true | false | (EXPR)`

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

Write = `write(EXPR)`

- Вся программа - это ST*

#Пример

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

x = 43

while(x) {
    function2(function);
}

return 42;
```