import os, zipfile

EXCLUDE_DIRS = {".git", ".gradle", ".idea", ".kotlin", ".claude", "build", "__pycache__"}
EXCLUDE_FILES = {".env", "local.properties", "CookingNote_SourceCode.zip", "CookingNote_BaoCao.docx"}

def make_source_zip(out_path="CookingNote_SourceCode.zip"):
    count = 0
    with zipfile.ZipFile(out_path, "w", zipfile.ZIP_DEFLATED) as zf:
        for root, dirs, files in os.walk("."):
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS and not (root == "." and d in {"docs", "scripts"})]
            if "build" in dirs:
                dirs.remove("build")
            for f in files:
                if f in EXCLUDE_FILES or f.endswith((".pyc", ".zip")):
                    continue
                abs_p = os.path.join(root, f)
                arc_p = os.path.relpath(abs_p, ".").replace(os.sep, "/")
                zf.write(abs_p, arc_p)
                count += 1
    return count

if __name__ == "__main__":
    c = make_source_zip()
    with zipfile.ZipFile("CookingNote_SourceCode.zip") as z:
        names = z.namelist()
        assert "app/src/main/AndroidManifest.xml" in names, "Missing manifest"
        assert "gradlew" in names, "Missing gradle wrapper"
        assert ".env" not in names and "local.properties" not in names, "Leaked secret/local config"
    print(f"OK: CookingNote_SourceCode.zip ({c} files, {os.path.getsize('CookingNote_SourceCode.zip') / 1024:.1f} KB)")
