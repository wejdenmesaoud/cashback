pipeline {
    agent any
    
    environment {
        // Use container name instead of localhost since Jenkins runs in Docker
        SONAR_HOST_URL = 'http://casecashback-sonarqube:9000'
        SONAR_LOGIN = 'admin'
        SONAR_PASSWORD = 'root'
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    // For local development, always use local files if available
                    if (fileExists('pom.xml')) {
                        echo "✅ Working with local files - skipping Git checkout"
                        echo "Local workspace detected with pom.xml present"
                        
                        // Show current directory contents for debugging
                        if (isUnix()) {
                            sh 'ls -la'
                            sh 'pwd'
                        } else {
                            bat 'dir'
                            bat 'cd'
                        }
                    } else {
                        echo "❌ No local files found, attempting Git checkout..."
                        
                        // Clean workspace before checkout
                        deleteDir()
                        
                        echo "Testing network connectivity to GitHub..."
                        
                        // Enhanced network connectivity test
                        try {
                            if (isUnix()) {
                                sh 'nslookup github.com || echo "DNS lookup failed"'
                                sh 'ping -c 3 github.com || echo "Ping failed"'
                                sh 'curl -v --connect-timeout 10 https://github.com || echo "HTTPS connection failed"'
                            } else {
                                bat 'nslookup github.com || echo "DNS lookup failed"'
                                bat 'ping -n 3 github.com || echo "Ping failed"'
                                bat 'curl -v --connect-timeout 10 https://github.com || echo "HTTPS connection failed"'
                            }
                        } catch (Exception e) {
                            echo "⚠️ Network connectivity test failed: ${e.getMessage()}"
                            echo "This might indicate firewall, proxy, or DNS issues"
                        }
                        
                        // Configure Git with more aggressive network settings
                        if (isUnix()) {
                            sh '''
                                git config --global http.postBuffer 1048576000
                                git config --global http.maxRequestBuffer 100M
                                git config --global core.preloadindex true
                                git config --global http.lowSpeedLimit 0
                                git config --global http.lowSpeedTime 999999
                                git config --global http.timeout 900
                                git config --global http.sslVerify false
                                git config --global core.compression 0
                            '''
                        } else {
                            bat '''
                                git config --global http.postBuffer 1048576000
                                git config --global http.maxRequestBuffer 100M
                                git config --global core.preloadindex true
                                git config --global http.lowSpeedLimit 0
                                git config --global http.lowSpeedTime 999999
                                git config --global http.timeout 900
                                git config --global http.sslVerify false
                                git config --global core.compression 0
                            '''
                        }
                        
                        // Try alternative approaches for checkout
                        def checkoutSuccess = false
                        
                        // Approach 1: Standard checkout with extended timeout
                        if (!checkoutSuccess) {
                            try {
                                timeout(time: 30, unit: 'MINUTES') {
                                    checkout([
                                        $class: 'GitSCM',
                                        branches: [[name: '*/main']],
                                        doGenerateSubmoduleConfigurations: false,
                                        extensions: [
                                            [$class: 'CloneOption', 
                                             depth: 0, 
                                             noTags: false, 
                                             reference: '', 
                                             shallow: false,
                                             timeout: 60],
                                            [$class: 'CheckoutOption', timeout: 60],
                                            [$class: 'CleanBeforeCheckout'],
                                            [$class: 'CleanCheckout']
                                        ],
                                        submoduleCfg: [],
                                        userRemoteConfigs: [[
                                            url: 'https://github.com/wejdenmesaoud/cashback.git'
                                        ]]
                                    ])
                                    checkoutSuccess = true
                                    echo "✅ Successfully cloned repository using standard method!"
                                }
                            } catch (Exception e) {
                                echo "❌ Standard checkout failed: ${e.getMessage()}"
                            }
                        }
                        
                        // Approach 2: Manual git clone as fallback
                        if (!checkoutSuccess) {
                            try {
                                echo "Attempting manual git clone..."
                                if (isUnix()) {
                                    sh 'git clone --depth 1 https://github.com/wejdenmesaoud/cashback.git .'
                                } else {
                                    bat 'git clone --depth 1 https://github.com/wejdenmesaoud/cashback.git .'
                                }
                                checkoutSuccess = true
                                echo "✅ Successfully cloned repository using manual method!"
                            } catch (Exception e) {
                                echo "❌ Manual git clone failed: ${e.getMessage()}"
                            }
                        }
                        
                        // Final fallback: Error with helpful message
                        if (!checkoutSuccess) {
                            error """
❌ All checkout methods failed. This appears to be a network connectivity issue.

Possible solutions:
1. Check if Jenkins can access the internet
2. Verify proxy settings if behind corporate firewall
3. Check if GitHub is accessible from your network
4. Consider using local file checkout instead
5. Verify the repository URL is correct

For local development, ensure your source files are available in the Jenkins workspace.
"""
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw clean compile'
                    } else {
                        bat 'mvnw.cmd clean compile'
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw test'
                    } else {
                        bat 'mvnw.cmd test'
                    }
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    script {
                        try {
                            publishCoverage adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                        } catch (Exception e) {
                            echo "Coverage publishing failed: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests'
                    } else {
                        bat 'mvnw.cmd package -DskipTests'
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    try {
                        if (isUnix()) {
                            sh """
                                ./mvnw sonar:sonar \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.login=${SONAR_LOGIN} \
                                -Dsonar.password=${SONAR_PASSWORD} \
                                -Dsonar.projectKey=cashback-security-jwt \
                                -Dsonar.projectName="Cashback Security JWT"
                            """
                        } else {
                            bat """
                                mvnw.cmd sonar:sonar ^
                                -Dsonar.host.url=%SONAR_HOST_URL% ^
                                -Dsonar.login=%SONAR_LOGIN% ^
                                -Dsonar.password=%SONAR_PASSWORD% ^
                                -Dsonar.projectKey=cashback-security-jwt ^
                                -Dsonar.projectName="Cashback Security JWT"
                            """
                        }
                        echo "SonarQube analysis completed successfully!"
                        echo "View results at: ${SONAR_HOST_URL}/dashboard?id=cashback-security-jwt"
                    } catch (Exception e) {
                        echo "SonarQube analysis failed: ${e.getMessage()}"
                        echo "This might be due to SonarQube not being fully ready or network issues."
                        echo "Pipeline will continue as UNSTABLE..."
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '🎉 Pipeline completed successfully!'
            echo "SonarQube Dashboard: http://localhost:9000"
        }
        failure {
            echo '❌ Pipeline failed!'
        }
        unstable {
            echo '⚠️ Pipeline completed with warnings (e.g., SonarQube analysis issues)'
        }
    }
}