# Jolt
A Lightweight, Modern, Fully-Featured Scripting Language
## Comments
A line comment starts with two forward-slashes.
```kotlin
// Line Comment
```
A block comment starts with forward-slash asterisk and ends with asterisk forward-slash.
```kotlin
/*
    Block
    Comment
*/
```
## Literals
### Booleans
A boolean literal is 'true' or 'false'.
```kotlin
true
false
```
### Numbers
Number literals are either integer literals,
```kotlin
0
1337
69420
```
... decimal literals,
```kotlin
0.0
12.5
110.48
```
... or engineering literals.
```kotlin
1E9
2.5e-7
3.9E+2
```
### Strings
String literals are any amount of characters between double-quotes.
```kotlin
"Hello, World!"
"ABCDEFG"
"   Woah   "
```
Certain important characters can be represented as escape sequences, using a backslash.

| Name            | Escape Sequence |
|-----------------|:---------------:|
| Null Terminator |      `\0`       |
| Alert           |      `\a`       |
| Backspace       |      `\b`       |
| Form Feed       |      `\f`       |
| Newline         |      `\n`       |
| Carriage Return |      `\r`       |
| Horizontal Tab  |      `\t`       |
| Vertical Tab    |      `\v`       |
| Backslash       |      `\\`       |
| Double Quote    |      `\"`       |
| ASCII           |     `\x--`      |
| Unicode         |    `\u----`     |
| Long Unicode    |  `\U--------`   |
| Name            |    `\(name)`    |
### Identifiers
Identifiers are any series of alphanumeric characters or underscores, preceded by and alphabetic character or underscore.
```kotlin
arg5
MAX_SIZE
_weird
```
## Expressions
### Precedence
|    Priority     | Name           |     Operators      | Associativity |
|:---------------:|----------------|:------------------:|---------------|
| 15<br>(Highest) | Access         |     `. [] ()`      | Right         |
|       14        | Postfix        |      `++ --`       | Right         |
|       13        | Prefix         |   `- # ! ++ --`    | Left          |
|       12        | Type Cast      |      `as as?`      | Right         |
|       11        | Multiplicative |      `* / %`       | Right         |
|       10        | Additive       |       `+ -`        | Right         |
|        9        | Range          |      `.. ...`      | Right         |
|        8        | Infix          |       `name`       | Right         |
|        7        | Coalesce       |        `?:`        | Right         |
|        6        | Named Checks   |  `in !in is !is`   | Right         |
|        5        | Comparison     |    `< <= > >=`     | Right         |
|        4        | Equality       |  `== != === !===`  | Right         |
|        3        | Conjunction    |        `&&`        | Right         |
|        2        | Disjunction    |       `\|\|`       | Right         |
|  1<br>(Lowest)  | Assignment     | `= += -= *= /= %=` | Left          |
### Access Operators
#### Member `a.b`
Accesses the member *b* from a class instance *a*.
#### Index `a[b]`
When *a* is a string, accesses the character at index `b` within the string.<br>
When *a* is a list, accesses the element at index `b` within the list.<br>
When *a* is a map, accesses the value at key `b` within the map.
#### Invoke `a(b,c...)`
Calls the given function or lambda function *a* with the given arguments *b*, *c*, etc.
### Postfix Operators
#### Post-Increment `a++`
Results in the current number stored by variable *a*, and increases the value by 1.
```kotlin
// x = 5
a   // 5
a++ // 5
a   // 6
```
#### Post-Decrement `a--`
Results in the current number stored by variable *a*, and decreases the value by 1.
```kotlin
// a = 5
a   // 5
a-- // 5
a   // 4
```
### Prefix Operators
#### Negate `-a`
When *a* is a number, results in the negative of *a*.<br>
When *a* is a string, results in the reverse of *a*.<br>
When *a* is a list, results in the reverse of *a*.
```kotlin
// a = 2
-a // -2

// b = -3
-b // 3

// c = "hello"
-c // "olleh"

// d = [1, 2, 3]
-d // [3, 2, 1]
```
#### Size `#a`
When *a* is a string, results in the length of *a*.<br>
When *a* is a list, results in the size of *a*.<br>
Otherwise, results in 1.
```kotlin
// a = "hello"
#a // 5

// b = [1, 2, 3]
#b // 3

// c = 2
#c // 1
```
#### Not `!a`
#### Pre-Increment `++a`
#### Pre-Decrement `--a`
### Type Cast Operators
#### Cast `a as type`
#### Safe Cast `a as? type`
### Multiplicative Operators
#### Multiply `a * b`
#### Divide `a / b`
#### Modulus `a % b`
### Additive Operators
#### Add `a + b`
#### Subtract `a - b`
### Range Operators
#### Exclusive `a..b`
#### Inclusive `a...b`
### Infix Operator `a name b`
### Coalesce Operator `a ?? b`
### Named Check Operators
#### In `a in b`
#### Not In `a !in b`
#### Is `a is type`
#### Not Is `a !is type`
### Comparison Operators
#### Less `a < b`
#### Less Equal `a <= b`
#### Greater `a > b`
#### Greater Equal `a >= b`
### Equality Operators
#### Equivalent `a == b`
#### Not Equivalent `a != b`
#### Equal `a === b`
#### Not Equal `a !== b`
### Conjunction Operator `&&`
### Disjunction Operator `||`
### Assignment Operators
#### Assign `=`
#### Add Assign `+=`
#### Subtract Assign `-=`
#### Multiply Assign `*=`
#### Divide Assign `/=`
#### Modulus Assign `%=`