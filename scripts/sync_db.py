import pymysql

# AWS (Source) database credentials
AWS_HOST = "scm-db.cbq80wao2479.ap-south-1.rds.amazonaws.com"
AWS_PORT = 3306
AWS_DB = "ngch_db"
AWS_USER = "admin"
AWS_PASSWORD = "Pavan9580"

# Local (Target) database credentials
LOCAL_HOST = "localhost"
LOCAL_PORT = 3306
LOCAL_DB = "ngch_db"
LOCAL_USER = "root"
LOCAL_PASSWORD = "Pavan@2006"

try:
    # Connect to AWS
    aws_conn = pymysql.connect(
        host=AWS_HOST,
        port=AWS_PORT,
        user=AWS_USER,
        password=AWS_PASSWORD,
        database=AWS_DB,
        cursorclass=pymysql.cursors.DictCursor
    )
    print("Connected to AWS RDS database.")

    # Connect to Local
    local_conn = pymysql.connect(
        host=LOCAL_HOST,
        port=LOCAL_PORT,
        user=LOCAL_USER,
        password=LOCAL_PASSWORD,
        database=LOCAL_DB
    )
    print("Connected to Local MySQL database.")

    # List of tables to sync in order
    tables = ["users", "contact", "direct_messages", "feedbacks", "social_link", "user_role_list"]

    with aws_conn.cursor() as aws_cursor, local_conn.cursor() as local_cursor:
        # Disable foreign key checks on local to allow clean truncation & inserts
        local_cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")
        
        for table in tables:
            print(f"Syncing table: {table}...")
            
            # Fetch data from AWS
            aws_cursor.execute(f"SELECT * FROM `{table}`;")
            rows = aws_cursor.fetchall()
            
            # Truncate local table
            local_cursor.execute(f"TRUNCATE TABLE `{table}`;")
            
            if not rows:
                print(f"Table {table} is empty. Truncated local table.")
                continue

            # Construct INSERT statement
            columns = list(rows[0].keys())
            placeholders = ", ".join(["%s"] * len(columns))
            sql = f"INSERT INTO `{table}` ({', '.join([f'`{col}`' for col in columns])}) VALUES ({placeholders});"
            
            # Convert dict rows to tuple of values
            val_list = []
            for row in rows:
                val_list.append(tuple(row[col] for col in columns))
                
            local_cursor.executemany(sql, val_list)
            print(f"Successfully copied {len(rows)} rows into local {table} table.")

        # Re-enable foreign key checks
        local_cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")
        local_conn.commit()
        print("\nSUCCESS: Sync completed! Local database is now identical to AWS database.")

except Exception as e:
    print(f"Error during sync: {e}")
finally:
    if 'aws_conn' in locals() and aws_conn.open:
        aws_conn.close()
    if 'local_conn' in locals() and local_conn.open:
        local_conn.close()
