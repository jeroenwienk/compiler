grammar Compiler;

program
  : statement*
  ;

statement
  :  blockStatement
  |  variableStatement
  |  assignmentStatement
  |  ifStatement
  |  iterationStatement
  |  printStatement
  ;

expression
  : '(' expression ')'                                                            #ParenthesesExpression
  | '!' expression                                                                #NotExpression
  | '-' expression                                                                #NegateExpression
  | left=expression op=('*' | '/') right=expression                               #MulDivExpression
  | left=expression op=( '+' | '-' ) right=expression                             #AddSubExpression
  | left=expression op=('<' | '<=' | '>' | '>=' | '==' | '!=' ) right=expression  #ComparisonExpression
  | left=expression op=('&&' | '||') right=expression                             #LogicalExpression
  | INT                                                                           #IntConstExpression
  | DOUBLE                                                                        #DoubleConstExpression
  | BOOLEAN                                                                       #BooleanConstExpression
  | IDENTIFIER                                                                    #VariableConstExpression
  ;

variableStatement
  : VAR assignmentStatement
  ;

assignmentStatement
  : IDENTIFIER EQUALS expression (';')?
  ;

ifStatement
  : IF '(' expression ')' statement ( ELSE statement )?
  ;

iterationStatement
  : WHILE '(' expression ')' statement                                               #WhileStatement
  | FOR '(' variableStatement ';' expression ';' assignmentStatement ')' statement     #ForStatement
  ;

printStatement
  : PRINT '(' expression ')' (';')?
  ;

statementList
  : statement+
  ;

blockStatement
  : '{' statementList? '}'
  ;

VAR : 'var';
IF : 'if';
ELSE : 'else';
WHILE: 'while';
FOR: 'for';
PRINT : 'print';
INT : '0' | [1-9][0-9]*;
DOUBLE: (INT) '.' (INT);
BOOLEAN: 'true' | 'false';
EQUALS : '=';
IDENTIFIER : [a-zA-Z] [a-zA-Z0-9_-]*;
WS : [ \t\n\r]+ -> skip;
