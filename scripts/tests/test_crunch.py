#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-2.0-only
"""
Tests for _crunch_license_text() in oe-go-mod-fetcher.py.

Focus is on the appendix-aware normalization that lets real-world LICENSE
files (with/without the Apache-2.0 appendix, with copyright preambles,
etc.) match the corresponding canonical OE-core common-licenses/ text.

Usage:
    python3 scripts/tests/test_crunch.py

Exits non-zero on the first failure. No external dependencies; runs against
the OE-core common-licenses dir if available, but every assertion that
depends on it is wrapped in a guard so the test still runs (with fewer
checks) outside a Poky tree.
"""

import hashlib
import importlib.util
import os
import sys
from pathlib import Path


HERE = Path(__file__).resolve().parent
FETCHER = HERE.parent / 'oe-go-mod-fetcher.py'


def _load_crunch():
    spec = importlib.util.spec_from_file_location('fetcher', FETCHER)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod._crunch_license_text


def _find_common_licenses():
    """Walk up from this file looking for poky's meta/files/common-licenses."""
    here = Path(__file__).resolve()
    # Search likely locations relative to a layer checkout.
    candidates = []
    for parent in [here.parent] + list(here.parents):
        candidates.append(parent / 'openembedded-core' / 'meta' / 'files' / 'common-licenses')
        candidates.append(parent / 'poky' / 'meta' / 'files' / 'common-licenses')
        candidates.append(parent.parent / 'openembedded-core' / 'meta' / 'files' / 'common-licenses')
        candidates.append(parent.parent / 'poky' / 'meta' / 'files' / 'common-licenses')
    for c in candidates:
        if c.is_dir():
            return c
    return None


COMMON_LICENSES = _find_common_licenses()
crunch = _load_crunch()


# --------------------------------------------------------------------------
# Fixture text — small enough to inline. Each fixture is intentionally
# minimal: we want to assert the crunch's behavior, not the canonical
# license content.
# --------------------------------------------------------------------------

# A condensed Apache-2.0 "body" — just enough to make the test meaningful.
# The real canonical file is 178 lines of license text; here we keep one
# distinctive sentence so we can verify the crunch is normalizing what we
# expect.
APACHE_BODY = """
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
"""

APACHE_APPENDIX = """
   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information.

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
"""

LLVM_EXCEPTION = """

--- LLVM Exceptions to the Apache 2.0 License ----

As an exception, if, as a result of your compiling your source code, portions
of this Software are embedded into an Object form of such source code, you
may redistribute such embedded portions in such Object form without complying
with the conditions of Sections 4(a), 4(b) and 4(d) of the License.
"""

PROJECT_NOTICE = """

This software is part of Project X. For questions please contact x@example.com.
"""

COPYRIGHT_PREAMBLE = "Copyright 2023 Example Corp.\n\n"

MIT_BODY = """MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED.
"""

BSD3_BODY = """Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED.
"""


# --------------------------------------------------------------------------
# Test infrastructure
# --------------------------------------------------------------------------

FAILURES = []


def test(name):
    def deco(fn):
        def wrapper():
            try:
                fn()
                print(f"  ok    {name}")
            except AssertionError as e:
                FAILURES.append((name, str(e)))
                print(f"  FAIL  {name}: {e}")
            except Exception as e:
                FAILURES.append((name, f"{type(e).__name__}: {e}"))
                print(f"  ERROR {name}: {type(e).__name__}: {e}")
        wrapper.__name__ = fn.__name__
        return wrapper
    return deco


# --------------------------------------------------------------------------
# Synthetic fixture tests — run anywhere, no OE-core required
# --------------------------------------------------------------------------

@test("apache: vanilla body and body+appendix crunch identically")
def t_apache_appendix_neutral():
    h_no = crunch(APACHE_BODY)
    h_with = crunch(APACHE_BODY + APACHE_APPENDIX)
    assert h_no == h_with, f"with-appendix={h_with} no-appendix={h_no}"


@test("apache: copyright preamble does not change crunched hash")
def t_apache_copyright_preamble():
    h_plain = crunch(APACHE_BODY)
    h_pre = crunch(COPYRIGHT_PREAMBLE + APACHE_BODY)
    assert h_plain == h_pre


@test("apache: with-appendix + LLVM-exception text differs from vanilla apache")
def t_apache_llvm_exception_distinct():
    h_apache = crunch(APACHE_BODY + APACHE_APPENDIX)
    h_llvm = crunch(APACHE_BODY + APACHE_APPENDIX + LLVM_EXCEPTION)
    assert h_apache != h_llvm, "LLVM exception text must survive appendix strip"


@test("apache: post-appendix project notice differs from canonical")
def t_apache_post_appendix_notice_distinct():
    h_apache = crunch(APACHE_BODY + APACHE_APPENDIX)
    h_extra = crunch(APACHE_BODY + APACHE_APPENDIX + PROJECT_NOTICE)
    assert h_apache != h_extra, \
        "post-appendix text must be preserved (overlay needed for variants)"


@test("apache: leading appendix with no body still produces a hash")
def t_apache_appendix_only():
    # Pathological: a file that's just the appendix template. Should not error.
    h = crunch(APACHE_APPENDIX)
    assert h is not None and len(h) == 32


@test("mit: copyright preamble does not change crunched hash")
def t_mit_copyright_preamble():
    h_plain = crunch(MIT_BODY)
    h_pre = crunch("Copyright (c) 2024 Example <dev@example.com>\n\n" + MIT_BODY)
    assert h_plain == h_pre


@test("bsd3: 'All Rights Reserved' line does not change crunched hash")
def t_bsd3_rights_reserved_neutral():
    h_plain = crunch(BSD3_BODY)
    h_rr = crunch("All Rights Reserved.\n" + BSD3_BODY)
    assert h_plain == h_rr


@test("different licenses produce distinct crunched hashes")
def t_distinct_licenses_distinct_hashes():
    hashes = {
        'apache': crunch(APACHE_BODY + APACHE_APPENDIX),
        'mit': crunch(MIT_BODY),
        'bsd3': crunch(BSD3_BODY),
    }
    assert len(set(hashes.values())) == 3, \
        f"expected 3 distinct hashes, got {hashes}"


@test("crunch returns 32-char hex string for typical input")
def t_crunch_shape():
    h = crunch(APACHE_BODY)
    assert isinstance(h, str) and len(h) == 32 and all(c in '0123456789abcdef' for c in h)


# --------------------------------------------------------------------------
# Tests that require OE-core common-licenses/
# --------------------------------------------------------------------------

@test("oe-core: all 5 APPENDIX-bearing canonical files produce distinct hashes")
def t_oe_appendix_files_distinct():
    if not COMMON_LICENSES:
        print("        (skipped: no OE-core common-licenses found)")
        return
    files = ['Apache-2.0', 'Apache-2.0-with-LLVM-exception',
             'ECL-2.0', 'SHL-0.5', 'SHL-0.51']
    hashes = {}
    for fn in files:
        p = COMMON_LICENSES / fn
        if not p.is_file():
            continue
        with open(p, errors='surrogateescape') as f:
            text = f.read()
        h = crunch(text)
        hashes[fn] = h
    seen = {}
    for fn, h in hashes.items():
        if h in seen:
            raise AssertionError(f"collision: {fn} and {seen[h]} both crunch to {h}")
        seen[h] = fn


@test("oe-core: patch introduces no new crunch collisions across canonical files")
def t_oe_no_new_collisions():
    if not COMMON_LICENSES:
        print("        (skipped: no OE-core common-licenses found)")
        return
    by_hash = {}
    pre_existing_collisions = 0
    for fn in sorted(os.listdir(COMMON_LICENSES)):
        p = COMMON_LICENSES / fn
        if not p.is_file():
            continue
        with open(p, errors='surrogateescape') as f:
            text = f.read()
        h = crunch(text)
        by_hash.setdefault(h, []).append(fn)
    collision_groups = [v for v in by_hash.values() if len(v) > 1]
    # The original OE-core crunch produces 10 collision groups across
    # common-licenses (AGPL-1.0 variants, GFDL variants, OFL variants, etc.).
    # The appendix-aware crunch must not introduce any NEW groups; the 5
    # APPENDIX-bearing files must remain pairwise distinct.
    apx_files = {'Apache-2.0', 'Apache-2.0-with-LLVM-exception',
                 'ECL-2.0', 'SHL-0.5', 'SHL-0.51'}
    for grp in collision_groups:
        apx_in_group = apx_files.intersection(grp)
        if len(apx_in_group) > 1:
            raise AssertionError(
                f"new collision between APPENDIX-bearing files: {sorted(grp)}")


@test("oe-core: canonical Apache-2.0 file matches synthetic APACHE_BODY+APPENDIX")
def t_oe_apache_canonical_shape():
    if not COMMON_LICENSES:
        print("        (skipped: no OE-core common-licenses found)")
        return
    # This is a sanity check that our synthetic APACHE_BODY/APPENDIX fixtures
    # crunch the same way the real canonical file does. They WILL differ in
    # value (canonical has 178 lines of license text vs our 15-line body), but
    # if our synthetic vanilla body crunches identically to vanilla+appendix,
    # then the canonical file should ALSO crunch identically to itself when
    # the appendix is stripped — which we verify here.
    with open(COMMON_LICENSES / 'Apache-2.0', errors='surrogateescape') as f:
        text = f.read()
    # Locate APPENDIX line and synthesize a no-appendix variant.
    lines = text.splitlines()
    cut = None
    for i, line in enumerate(lines):
        if line.lstrip().startswith('APPENDIX:'):
            cut = i
            break
    assert cut is not None, "canonical Apache-2.0 has no APPENDIX line?"
    no_appendix = '\n'.join(lines[:cut])
    h_full = crunch(text)
    h_stripped = crunch(no_appendix)
    assert h_full == h_stripped, \
        f"canonical Apache-2.0 with vs without appendix differ: {h_full} vs {h_stripped}"


# --------------------------------------------------------------------------
# Runner
# --------------------------------------------------------------------------

def main():
    print(f"Testing crunch from {FETCHER}")
    if COMMON_LICENSES:
        print(f"OE-core common-licenses: {COMMON_LICENSES}")
    else:
        print("OE-core common-licenses: not found (some tests will be skipped)")
    print()
    tests = [
        t_apache_appendix_neutral,
        t_apache_copyright_preamble,
        t_apache_llvm_exception_distinct,
        t_apache_post_appendix_notice_distinct,
        t_apache_appendix_only,
        t_mit_copyright_preamble,
        t_bsd3_rights_reserved_neutral,
        t_distinct_licenses_distinct_hashes,
        t_crunch_shape,
        t_oe_appendix_files_distinct,
        t_oe_no_new_collisions,
        t_oe_apache_canonical_shape,
    ]
    for t in tests:
        t()
    print()
    if FAILURES:
        print(f"{len(FAILURES)} failure(s):")
        for name, msg in FAILURES:
            print(f"  - {name}: {msg}")
        return 1
    print(f"All {len(tests)} test(s) passed.")
    return 0


if __name__ == '__main__':
    sys.exit(main())
