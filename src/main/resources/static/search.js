document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('search');
    const commandSections = document.querySelectorAll('.command-section');

    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        
        commandSections.forEach(section => {
            const commandName = section.querySelector('h2').textContent.toLowerCase();
            const commandDescription = section.querySelector('.description').textContent.toLowerCase();
            const commandOptions = Array.from(section.querySelectorAll('td')).map(td => td.textContent.toLowerCase());
            
            const matches = commandName.includes(searchTerm) || 
                          commandDescription.includes(searchTerm) ||
                          commandOptions.some(option => option.includes(searchTerm));
            
            section.style.display = matches ? 'block' : 'none';
        });
    });
}); 