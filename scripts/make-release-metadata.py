"""Build update manifest from the same version properties Gradle uses."""
import hashlib, json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
props = dict(line.strip().split('=', 1) for line in (root / 'version.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
apk = root / 'release' / 'Pharmnote.apk'
result = {'applicationId': 'com.pharmnote.study', 'versionCode': int(props['VERSION_CODE']), 'versionName': props['VERSION_NAME'], 'minSdk': 26, 'sha256': hashlib.sha256(apk.read_bytes()).hexdigest()}
(apk.parent / 'update.json').write_text(json.dumps(result, indent=2) + '\n')
