#!/usr/bin/env python3
"""
Automated fix for Spring Data JPA Optional<T> handling issues in Java 21 migration.
Fixes the common pattern where findById() now returns Optional<T> instead of T.

Author: AI Assistant
Date: 2025-11-08
"""

import os
import re
import sys
from pathlib import Path
from typing import List, Tuple

class OptionalFixer:
    def __init__(self, src_dir: str = "src"):
        self.src_dir = src_dir
        self.fixed_files = []
        self.errors = []
        
    def find_java_files(self) -> List[Path]:
        """Find all Java files in src directory"""
        return list(Path(self.src_dir).rglob("*.java"))
    
    def fix_assignment_pattern(self, content: str) -> Tuple[str, int]:
        """
        Fix pattern: Type var = repo.findById(id);
        Replace with: Type var = repo.findById(id).orElse(null);
        """
        # Pattern matches: TypeName varName = somethingRepository.findById(...);
        pattern = r'(\s+)(\w+(?:<[^>]+>)?)\s+(\w+)\s*=\s*(\w+Repository)\.findById\(([^)]+)\);'
        
        def replacement(match):
            indent = match.group(1)
            type_name = match.group(2)
            var_name = match.group(3)
            repo_name = match.group(4)
            id_param = match.group(5)
            
            return f'{indent}{type_name} {var_name} = {repo_name}.findById({id_param}).orElse(null);'
        
        new_content, count = re.subn(pattern, replacement, content)
        return new_content, count
    
    def fix_direct_usage(self, content: str) -> Tuple[str, int]:
        """
        Fix pattern: method(repo.findById(id))
        Replace with: method(repo.findById(id).orElse(null))
        
        Only fixes if not already followed by .orElse, .get, .map, etc.
        """
        # Match findById(...) not followed by a dot (method call)
        pattern = r'\.findById\(([^)]+)\)(?!\s*\.)'
        
        def replacement(match):
            id_param = match.group(1)
            return f'.findById({id_param}).orElse(null)'
        
        new_content, count = re.subn(pattern, replacement, content)
        return new_content, count
    
    def fix_inline_conditional(self, content: str) -> Tuple[str, int]:
        """
        Fix pattern: if (repo.findById(id) != null)
        Replace with: if (repo.findById(id).orElse(null) != null)
        """
        pattern = r'if\s*\(\s*(\w+Repository)\.findById\(([^)]+)\)\s*(!=|==)\s*null\s*\)'
        
        def replacement(match):
            repo_name = match.group(1)
            id_param = match.group(2)
            operator = match.group(3)
            
            return f'if ({repo_name}.findById({id_param}).orElse(null) {operator} null)'
        
        new_content, count = re.subn(pattern, replacement, content)
        return new_content, count
    
    def fix_return_statement(self, content: str) -> Tuple[str, int]:
        """
        Fix pattern: return repo.findById(id);
        Replace with: return repo.findById(id).orElse(null);
        """
        pattern = r'return\s+(\w+Repository)\.findById\(([^)]+)\);'
        
        def replacement(match):
            repo_name = match.group(1)
            id_param = match.group(2)
            
            return f'return {repo_name}.findById({id_param}).orElse(null);'
        
        new_content, count = re.subn(pattern, replacement, content)
        return new_content, count
    
    def fix_method_parameter(self, content: str) -> Tuple[str, int]:
        """
        Fix pattern where findById is used as method argument spread across lines
        """
        # Look for patterns like: someMethod(\n  repo.findById(id)\n)
        pattern = r'(\w+Repository)\.findById\(([^)]+)\)(\s*[,)])'
        
        def replacement(match):
            repo_name = match.group(1)
            id_param = match.group(2)
            trailing = match.group(3)
            
            # Don't fix if already has .orElse or similar
            return f'{repo_name}.findById({id_param}).orElse(null){trailing}'
        
        # First check if the line already has orElse
        lines = content.split('\n')
        new_lines = []
        for line in lines:
            if '.findById(' in line and 'Repository' in line and '.orElse' not in line and '.get()' not in line:
                line, _ = re.subn(pattern, replacement, line)
            new_lines.append(line)
        
        new_content = '\n'.join(new_lines)
        count = 1 if new_content != content else 0
        return new_content, count
    
    def add_optional_import(self, content: str) -> str:
        """Add Optional import if not present and if needed"""
        if 'import java.util.Optional;' in content:
            return content
        
        # Check if we're using Optional
        if '.orElse(' in content or 'Optional<' in content:
            # Find the import section
            import_section_end = content.find('\npublic class')
            if import_section_end == -1:
                import_section_end = content.find('\npublic interface')
            if import_section_end == -1:
                import_section_end = content.find('\nclass')
            
            if import_section_end != -1:
                # Insert before class declaration
                lines = content[:import_section_end].split('\n')
                # Find last import
                last_import_idx = -1
                for i, line in enumerate(lines):
                    if line.strip().startswith('import '):
                        last_import_idx = i
                
                if last_import_idx != -1:
                    lines.insert(last_import_idx + 1, 'import java.util.Optional;')
                    content = '\n'.join(lines) + content[import_section_end:]
        
        return content
    
    def fix_file(self, filepath: Path) -> bool:
        """Fix a single Java file"""
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            total_fixes = 0
            
            # Apply all fix patterns
            content, count = self.fix_assignment_pattern(content)
            total_fixes += count
            
            content, count = self.fix_direct_usage(content)
            total_fixes += count
            
            content, count = self.fix_inline_conditional(content)
            total_fixes += count
            
            content, count = self.fix_return_statement(content)
            total_fixes += count
            
            content, count = self.fix_method_parameter(content)
            total_fixes += count
            
            # Add Optional import if needed
            if total_fixes > 0:
                content = self.add_optional_import(content)
            
            # Write back if changed
            if content != original_content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                self.fixed_files.append((str(filepath), total_fixes))
                return True
            
            return False
            
        except Exception as e:
            self.errors.append((str(filepath), str(e)))
            return False
    
    def run(self) -> dict:
        """Run the fixer on all Java files"""
        print("🔍 Scanning for Java files...")
        java_files = self.find_java_files()
        print(f"   Found {len(java_files)} Java files\n")
        
        print("🔧 Fixing Optional<T> handling issues...")
        for filepath in java_files:
            self.fix_file(filepath)
        
        return {
            'total_files': len(java_files),
            'fixed_files': len(self.fixed_files),
            'errors': len(self.errors)
        }
    
    def print_report(self, stats: dict):
        """Print summary report"""
        print("\n" + "="*60)
        print("📊 OPTIONAL<T> FIX REPORT")
        print("="*60)
        print(f"Total Java files scanned: {stats['total_files']}")
        print(f"Files modified: {stats['fixed_files']}")
        print(f"Errors encountered: {stats['errors']}")
        print()
        
        if self.fixed_files:
            print("✅ Modified files:")
            for filepath, count in sorted(self.fixed_files, key=lambda x: x[1], reverse=True):
                print(f"   {filepath} ({count} fixes)")
        
        if self.errors:
            print("\n❌ Errors:")
            for filepath, error in self.errors:
                print(f"   {filepath}: {error}")
        
        print("\n" + "="*60)
        print("✨ DONE! Run 'mvn compile -DskipTests' to check results")
        print("="*60)

def main():
    """Main entry point"""
    print("🚀 Optional<T> Handling Fixer for Java 21 Migration")
    print("   Spring Data JPA findById() now returns Optional<T>\n")
    
    fixer = OptionalFixer()
    stats = fixer.run()
    fixer.print_report(stats)
    
    return 0 if stats['errors'] == 0 else 1

if __name__ == "__main__":
    sys.exit(main())
