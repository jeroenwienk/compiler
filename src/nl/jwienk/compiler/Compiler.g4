grammar Compiler;


program : statement*  ;


statement : block              # BlockStatement
  |  variableStat              # VariableStatement
  |  ifStat                    # IfStatement
  |  printStat                 # PrintStatement
  ;


expression : '(' expression ')'                                                   #ParenthesesExpression
  | '-' expression                                                                #NegateExpression
  | left=expression op=('*' | '/') right=expression                               #MulDivExpression
  | left=expression op=( '+' | '-' ) right=expression                             #AddSubExpression
  | left=expression op=('<' | '<=' | '>' | '>=' | '==' | '!=' ) right=expression  #BooleanExpression
  | INT                                                                           #IntConstExpression
  | DOUBLE                                                                        #DoubleConstExpression
  | BOOLEAN                                                                       #BooleanConstExpression
  | IDENTIFIER                                                                    #VariableConstExpression
  ;

variableStat
  : IDENTIFIER EQUALS expression ';'
  ;


ifStat
  : IF '(' expression ')' statement ( ELSE statement )?
  ;

printStat
  : PRINT '(' expression ');'
  ;

statementList
  : statement+
  ;

block
  : '{' statementList? '}'
  ;

IF : 'if';
ELSE : 'else';
PRINT : 'print';
INT : '0' | [1-9][0-9]*;
DOUBLE: ('0' | [1-9][0-9]*) '.' ('0' | [1-9][0-9]*);
BOOLEAN: 'true' | 'false' | '1' | '0';
EQUALS : '=';
IDENTIFIER : [a-zA-Z] [a-zA-Z0-9_-]*;
WS : [ \t\n\r]+ -> skip;
