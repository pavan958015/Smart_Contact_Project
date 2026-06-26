# Compile the project and create the JAR file
Write-Host "Building project with Maven..." -ForegroundColor Green
.\mvnw clean package -DskipTests

# Check if build succeeded
if ($LASTEXITCODE -eq 0) {
    Write-Host "Build Successful. Packaging deployment ZIP..." -ForegroundColor Green
    
    # Copy and rename target jar to scm2.0.jar at root temporarily
    Copy-Item target/scm2.0-0.0.1-SNAPSHOT.jar scm2.0.jar
    
    # Create dist folder if it doesn't exist
    if (!(Test-Path -Path dist)) {
        New-Item -ItemType Directory -Path dist
    }
    
    # Create the zip containing the jar and Procfile
    Compress-Archive -Path scm2.0.jar, Procfile -DestinationPath dist/scm2.0-deployment.zip -Force
    
    # Clean up temporary root jar
    Remove-Item scm2.0.jar
    
    Write-Host "Deployment package created at dist/scm2.0-deployment.zip" -ForegroundColor Green
} else {
    Write-Warning "Build Failed! Please check compilation errors."
}
