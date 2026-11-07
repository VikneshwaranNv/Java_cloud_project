# Zero-Cost Cloud Linux VM Demonstration Guide
## Running Student 1's Traffic Engine in the Cloud (No Credit Card Required)

If your professors or examiners want to see the application running on a live remote **cloud Linux terminal** today, you can use **Google Cloud Shell** or **GitHub Codespaces**. Both provide real Linux cloud virtual machines for **$0 (100% Free Forever, with NO credit card or billing setup required)**.

---

## Method 1: Google Cloud Shell (Instant 60-Second Setup)

Google Cloud Shell gives every Google user a free, persistent Linux cloud virtual machine with a web-based terminal and pre-installed Java.

### Steps:
1. Open your browser and go to: **[shell.cloud.google.com](https://shell.cloud.google.com)**
2. Sign in with your regular Gmail or college Google account.
3. Once the black cloud terminal loads:
   - Click the **Three Dots (⋮)** in the top-right corner of the Cloud Shell window $\rightarrow$ Select **Upload File**.
   - Browse to `F:\Team Java\oci_vm1_bundle.tar.gz` and click **Upload**.
4. In the Cloud Shell terminal, run:
   ```bash
   # 1. Extract the bundle
   mkdir -p smart-traffic && tar -xzvf oci_vm1_bundle.tar.gz -C smart-traffic/
   cd smart-traffic

   # 2. Make launcher executable
   chmod +x deploy_oci_vm1.sh

   # 3. Run the automated traffic engine in the cloud!
   ./deploy_oci_vm1.sh --auto
   ```
5. You will see the entire Smart City Traffic Signal preemption, multithreaded workers, and corridor manifests running live on a real Google Cloud Linux VM!

---

## Method 2: GitHub Codespaces (Free Cloud IDE & Linux Terminal)

Every free GitHub account includes 60 hours per month of free cloud Linux compute.

### Steps:
1. Push your project from your PC to GitHub:
   ```cmd
   cd /d "F:\Team Java"
   git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/SmartTrafficRouter.git
   git branch -M main
   git push -u origin main
   ```
2. Open your repository on **GitHub.com**.
3. Click the green **`<> Code`** button $\rightarrow$ Select the **Codespaces** tab $\rightarrow$ Click **Create codespace on main**.
4. Within 15 seconds, a full VS Code IDE running inside a remote Linux cloud VM opens in your browser.
5. In the Codespaces terminal, run:
   ```bash
   mvn test
   java -jar target/SmartTrafficRouter-1.0.0.jar --auto
   ```

---

## Why This Gives You Full Marks:

Whether you demonstrate:
- **On your local PC:** Shows Java 21, multithreading, scoring algorithms, and local MySQL persistence.
- **On Google Cloud Shell:** Shows live remote Linux cloud execution without needing a credit card.
- **Tomorrow with Student 2 on OCI:** Shows multi-tier cloud networking and remote database hosting.

All options run the exact same `SmartTrafficRouter-1.0.0.jar`!
