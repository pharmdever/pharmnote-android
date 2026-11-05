"""Decode one GitHub Secret, verify its public certificate, and prepare Gradle env.
Never commit the input secret, keystore, or environment file.
"""
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess


def prepare():
    raw = os.environ.get('SIGNING_BUNDLE', '')
    if not raw:
        raise ValueError('Create the ANDROID_SIGNING_BUNDLE repository secret first.')
    if len(raw) > 60000:
        raise ValueError('Signing bundle exceeds supported secret size.')
    b = json.loads(raw)
    for key in ('storePassword', 'keyAlias', 'keyPassword', 'keystoreBase64'):
        if not isinstance(b.get(key), str) or not b[key] or '\n' in b[key] or '\r' in b[key]:
            raise ValueError('Invalid signing bundle field: ' + key)
    if not re.fullmatch(r'[A-Za-z0-9_.-]+', b['keyAlias']):
        raise ValueError('Invalid key alias.')
    key_bytes = base64.b64decode(b['keystoreBase64'], validate=True)
    if len(key_bytes) > 40000:
        raise ValueError('Keystore too large.')
    path = Path(os.environ['RUNNER_TEMP']) / 'pharmnote.jks'
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(fd, 'wb') as stream:
        stream.write(key_bytes)
    env = os.environ.copy()
    env['ANDROID_STORE_PASSWORD'] = b['storePassword']
    result = subprocess.run(
        ['keytool', '-exportcert', '-keystore', str(path),
         '-storepass:env', 'ANDROID_STORE_PASSWORD', '-alias', b['keyAlias']],
        env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
    )
    if result.returncode:
        raise ValueError('Cannot open signing key with the supplied credentials.')
    expected = (Path(__file__).resolve().parents[1] / 'signing-certificate.sha256').read_text().strip()
    if not re.fullmatch(r'[a-f0-9]{64}', expected):
        raise ValueError('Invalid pinned signing fingerprint.')
    if hashlib.sha256(result.stdout).hexdigest() != expected:
        raise ValueError('Certificate does not match the original signing key. Refusing to release.')
    for name in ('storePassword', 'keyPassword', 'keystoreBase64'):
        print('::add-mask::' + b[name])
    values = {
        'ANDROID_KEYSTORE_PATH': str(path),
        'ANDROID_STORE_PASSWORD': b['storePassword'],
        'ANDROID_KEY_ALIAS': b['keyAlias'],
        'ANDROID_KEY_PASSWORD': b['keyPassword'],
    }
    with open(os.environ['GITHUB_ENV'], 'a') as stream:
        for name, value in values.items():
            stream.write(name + '=' + value + '\n')
    print('Signing certificate verified against the original release key.')


if __name__ == '__main__':
    try:
        prepare()
    except Exception:
        # Input may contain secrets. Do not print exception strings or tracebacks.
        raise SystemExit('Signing setup failed. Check the private signing bundle and pinned certificate; no secrets were logged.')
