source oraenv

sqlplus / as sysdba << EOF
      ALTER SESSION SET CONTAINER = skyroof;
      CREATE USER skyroof IDENTIFIED BY test1234 CONTAINER=CURRENT;
      GRANT CREATE SESSION TO skyroof CONTAINER=CURRENT;
      exit;
EOF