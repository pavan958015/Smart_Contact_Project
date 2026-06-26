import pymysql

# Local credentials from application.properties
HOST = "localhost"
PORT = 3306
USER = "root"
PASSWORD = "Pavan@2006"

try:
    connection = pymysql.connect(
        host=HOST,
        port=PORT,
        user=USER,
        password=PASSWORD
    )
    print("SUCCESS: Successfully connected to local MySQL database!")
    with connection.cursor() as cursor:
        cursor.execute("SHOW DATABASES;")
        dbs = cursor.fetchall()
        print("Available local databases:")
        for db in dbs:
            print(f" - {db[0] if isinstance(db, tuple) else list(db.values())[0]}")
except Exception as e:
    print(f"FAILED: Could not connect to local MySQL: {e}")
finally:
    if 'connection' in locals() and connection.open:
        connection.close()
