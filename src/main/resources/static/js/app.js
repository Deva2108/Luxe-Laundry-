const API_BASE = '/api';
const AUTH_HEADER = 'Basic ' + btoa('admin:admin');

let currentView = 'dashboard';
let searchTimeout = null;
let currentOrderId = null;
let garmentLibrary = [];
let shopProfile = { shopName: 'LuxeLaundry', currencySymbol: '₹', taxPercentage: 0 };
let revenueChart = null;
let serviceChart = null;

const CATEGORIES = ['TOPWEAR', 'BOTTOMWEAR', 'BEDDING', 'DELICATES', 'ETHNIC', 'OTHERS'];
const SERVICES = ['WASH_FOLD', 'WASH_IRON', 'DRY_CLEANING'];
const STATUSES = ['RECEIVED', 'PROCESSING', 'READY', 'DELIVERED'];

// --- Utility: Toast Notifications ---
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    const colors = type === 'success' ? 'bg-emerald-500' : 'bg-rose-500';
    toast.className = `${colors} text-white px-6 py-4 rounded-2xl shadow-2xl flex items-center gap-3 toast-animate font-bold text-sm min-w-[300px]`;
    toast.innerHTML = `<i data-lucide="${type === 'success' ? 'check-circle' : 'alert-circle'}" class="w-5 h-5"></i> ${message}`;
    container.appendChild(toast);
    lucide.createIcons();
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

document.addEventListener('DOMContentLoaded', async () => {
    await loadProfile();
    showView('dashboard');
    addItemRow();
    loadLibrary();
    const priorityToggle = document.getElementById('is-priority');
    if (priorityToggle) priorityToggle.addEventListener('change', updateLiveBill);
    const setCat = document.getElementById('set-category');
    if(setCat) setCat.innerHTML = CATEGORIES.map(c => `<option value="${c}">${c}</option>`).join('');
});

async function apiFetch(endpoint, options = {}) {
    const headers = { 'Authorization': AUTH_HEADER, 'Content-Type': 'application/json', ...options.headers };
    const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    
    if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'API Error');
    }

    const contentType = response.headers.get("content-type");
    if (response.status === 204 || !contentType || !contentType.includes("application/json")) {
        return {};
    }
    
    return response.json();
}

async function loadProfile() {
    try {
        shopProfile = await apiFetch('/profile');
        const brand = document.getElementById('brand-name');
        if (brand) brand.textContent = shopProfile.shopName;
        
        const sideAdmin = document.getElementById('sidebar-admin-name');
        const sideInitials = document.getElementById('sidebar-admin-initials');
        const sideStatus = document.getElementById('sidebar-system-status');
        const owner = shopProfile.ownerName || 'Admin';
        
        if (sideAdmin) sideAdmin.textContent = owner;
        if (sideInitials) sideInitials.textContent = owner.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
        
        const adminData = await apiFetch('/profile/admin/info');
        if (sideStatus) {
            sideStatus.textContent = adminData.systemStatus === 'HEALTHY' ? '● Online' : '● ' + adminData.systemStatus;
            sideStatus.className = `text-[10px] ${adminData.systemStatus === 'HEALTHY' ? 'text-emerald-500' : 'text-amber-500'} font-medium flex items-center gap-1`;
        }

        if (document.getElementById('prof-shop-name')) {
            document.getElementById('prof-shop-name').value = shopProfile.shopName;
            document.getElementById('prof-owner-name').value = shopProfile.ownerName || '';
            document.getElementById('prof-email').value = shopProfile.email || '';
            document.getElementById('prof-phone').value = shopProfile.phoneNumber || '';
            document.getElementById('prof-address').value = shopProfile.address || '';
            document.getElementById('prof-currency').innerHTML = ['₹','$', '£', '€'].map(s => `<option value="${s}" ${s === shopProfile.currencySymbol ? 'selected' : ''}>${s}</option>`).join('');
            document.getElementById('prof-tax').value = shopProfile.taxPercentage || 0;
        }

        if (document.getElementById('admin-user-display')) {
            document.getElementById('admin-user-display').textContent = `${adminData.username} (${adminData.role})`;
            document.getElementById('admin-last-login').textContent = adminData.lastLogin;
            document.getElementById('admin-system-status').textContent = adminData.systemStatus;
        }
        updateLiveBill();
    } catch (e) { console.error("Profile load failed", e); }
}

async function handleSaveProfile(e) {
    e.preventDefault();
    const payload = {
        shopName: document.getElementById('prof-shop-name').value,
        ownerName: document.getElementById('prof-owner-name').value,
        email: document.getElementById('prof-email').value,
        phoneNumber: document.getElementById('prof-phone').value,
        address: document.getElementById('prof-address').value,
        currencySymbol: document.getElementById('prof-currency').value,
        taxPercentage: parseFloat(document.getElementById('prof-tax').value) || 0
    };
    try {
        await apiFetch('/profile', { method: 'POST', body: JSON.stringify(payload) });
        await loadProfile();
        showToast('Business workspace updated!');
    } catch (e) { showToast(e.message, 'error'); }
}

async function loadLibrary() {
    try {
        garmentLibrary = await apiFetch('/garments/library');
        const datalist = document.getElementById('garments-list');
        if (datalist) datalist.innerHTML = garmentLibrary.map(g => `<option value="${g.garmentName}">`).join('');
        const tbody = document.getElementById('settings-table');
        if (tbody) {
            tbody.innerHTML = garmentLibrary.length ? garmentLibrary.map(g => `
                <tr class="tr-hover">
                    <td class="p-6 text-sm font-bold text-slate-800">${g.garmentName}</td>
                    <td class="p-6"><span class="px-3 py-1 bg-slate-100 text-slate-500 rounded-lg text-[10px] font-bold uppercase">${g.defaultCategory}</span></td>
                    <td class="p-6 text-sm font-black text-blue-600">${shopProfile.currencySymbol}${g.defaultPrice.toFixed(2)}</td>
                    <td class="p-6 text-center">
                        <button onclick="deleteFromLibrary(${g.id})" class="text-slate-300 hover:text-rose-500 transition-colors"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                    </td>
                </tr>
            `).join('') : '<tr><td colspan="4" class="p-20 text-center text-slate-400 font-medium">No presets saved in library yet.</td></tr>';
            lucide.createIcons();
        }
    } catch (e) { console.error(e); }
}

async function handleSaveSetting(e) {
    e.preventDefault();
    const payload = {
        garmentName: document.getElementById('set-garment-name').value,
        defaultCategory: document.getElementById('set-category').value,
        defaultPrice: parseFloat(document.getElementById('set-price').value)
    };
    try {
        await apiFetch('/garments/library', { method: 'POST', body: JSON.stringify(payload) });
        document.getElementById('setting-form').reset();
        await loadLibrary();
        showToast('Library updated!');
    } catch (e) { showToast(e.message, 'error'); }
}

async function deleteFromLibrary(id) {
    if (!confirm('Confirm deletion?')) return;
    try {
        await apiFetch(`/garments/library/${id}`, { method: 'DELETE' });
        await loadLibrary();
        showToast('Item removed');
    } catch (e) { showToast(e.message, 'error'); }
}

function handleGarmentInput(input) {
    const val = input.value.trim().toLowerCase();
    const match = garmentLibrary.find(g => g.garmentName.toLowerCase() === val);
    if (match) {
        const row = input.closest('.item-row');
        if (row) {
            row.querySelector('.item-category').value = match.defaultCategory;
            row.querySelector('.item-price').value = match.defaultPrice;
        }
    }
    updateLiveBill();
}

function addItemRow() {
    const container = document.getElementById('items-container');
    if (!container) return;
    const row = document.createElement('div');
    row.className = 'grid grid-cols-12 gap-4 p-4 bg-slate-50/50 rounded-2xl border border-slate-100 item-row animate-slideUp';
    row.innerHTML = `
        <div class="col-span-3"><input type="text" list="garments-list" placeholder="Garment Name" class="w-full p-3 bg-white border border-slate-100 rounded-xl outline-none font-bold text-sm" required oninput="handleGarmentInput(this)"></div>
        <div class="col-span-2"><select class="item-category w-full p-3 bg-white border border-slate-100 rounded-xl text-xs font-bold">${CATEGORIES.map(c => `<option value="${c}">${c}</option>`).join('')}</select></div>
        <div class="col-span-3"><select class="item-service w-full p-3 bg-white border border-slate-100 rounded-xl text-xs font-bold">${SERVICES.map(s => `<option value="${s}">${s.replace('_', ' ')}</option>`).join('')}</select></div>
        <div class="col-span-1"><input type="number" value="1" min="1" class="item-qty w-full p-3 bg-white border border-slate-100 rounded-xl font-bold text-sm" oninput="updateLiveBill()"></div>
        <div class="col-span-2"><input type="number" placeholder="Rate" class="item-price w-full p-3 bg-white border border-slate-100 rounded-xl font-bold text-sm" oninput="updateLiveBill()"></div>
        <div class="col-span-1 flex items-center justify-center"><button type="button" onclick="this.closest('.item-row').remove(); updateLiveBill();" class="text-slate-300 hover:text-rose-500 transition-colors"><i data-lucide="trash-2" class="w-5 h-5"></i></button></div>
    `;
    container.appendChild(row);
    lucide.createIcons();
}

function updateLiveBill() {
    let base = 0;
    document.querySelectorAll('.item-row').forEach(row => {
        const qty = parseFloat(row.querySelector('.item-qty').value) || 0;
        const price = parseFloat(row.querySelector('.item-price').value) || 0;
        base += qty * price;
    });
    const priorityChecked = document.getElementById('is-priority') ? document.getElementById('is-priority').checked : false;
    const subtotal = base + (priorityChecked ? base * 0.2 : 0);
    const discountEl = document.getElementById('discount-pct');
    const discount = Math.min(parseFloat(discountEl ? discountEl.value : 0) || 0, 100);
    const taxable = subtotal - (subtotal * (discount / 100));
    const tax = taxable * ((shopProfile.taxPercentage || 0) / 100);
    const final = taxable + tax;
    
    const submitBtn = document.querySelector('button[type="submit"] span');
    if (submitBtn) {
        submitBtn.textContent = `CONFIRM BOOKING (${shopProfile.currencySymbol || '₹'}${final.toFixed(2)})`;
    }
}

async function handleCreateOrder(e) {
    e.preventDefault();
    const items = Array.from(document.querySelectorAll('.item-row')).map(row => ({
        garmentName: row.querySelector('input[type="text"]').value,
        category: row.querySelector('.item-category').value,
        serviceType: row.querySelector('.item-service').value,
        quantity: parseInt(row.querySelector('.item-qty').value),
        pricePerItem: parseFloat(row.querySelector('.item-price').value)
    }));
    const payload = {
        customerName: document.getElementById('cust-name').value,
        phoneNumber: document.getElementById('cust-phone').value,
        isPriority: document.getElementById('is-priority').checked,
        discountPercentage: parseFloat(document.getElementById('discount-pct').value) || 0,
        items: items
    };
    try {
        const response = await apiFetch('/orders', { method: 'POST', body: JSON.stringify(payload) });
        showToast('Booking Successful!');
        e.target.reset();
        document.getElementById('items-container').innerHTML = '';
        addItemRow();
        updateLiveBill();
        if (response.orderId) openOrderDetails(response.orderId);
        else showView('dashboard');
    } catch (e) { showToast(e.message, 'error'); }
}

async function openOrderDetails(orderId) {
    try {
        const order = await apiFetch(`/orders/${orderId}`);
        currentOrderId = orderId;
        const sym = shopProfile.currencySymbol;
        const modalId = document.getElementById('modal-order-id');
        if (modalId) modalId.textContent = `#${order.orderId}`;
        
        const totalContainer = document.getElementById('modal-total');
        if (totalContainer) {
            totalContainer.innerHTML = `
                <div class="space-y-4">
                    <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100 space-y-2">
                        <div class="flex justify-between text-[10px] font-bold text-slate-400 uppercase"><span>Expected Delivery</span><span class="text-slate-900">${order.estimatedDeliveryDate}</span></div>
                        <div class="flex justify-between text-[10px] font-bold text-slate-400 uppercase"><span>Priority Service</span><span class="${order.isPriority ? 'text-rose-600' : 'text-slate-600'}">${order.isPriority ? 'YES (EXPRESS)' : 'STANDARD'}</span></div>
                    </div>
                    <div class="space-y-2">
                        <div class="flex justify-between text-xs font-medium text-slate-400"><span>Original Total</span><span>${sym}${order.totalBill.toFixed(2)}</span></div>
                        <div class="flex justify-between text-xs font-bold text-rose-500"><span>Discount Applied</span><span>-${sym}${order.discountAmount.toFixed(2)}</span></div>
                        <div class="flex justify-between text-xl font-black text-slate-900 pt-2 border-t border-slate-50"><span>Payable Amount</span><span class="text-blue-600">${sym}${order.finalBill.toFixed(2)}</span></div>
                    </div>
                </div>
            `;
        }

        const statusSelect = document.getElementById('update-status-select');
        if (statusSelect) statusSelect.innerHTML = STATUSES.map(s => `<option value="${s}" ${order.status === s ? 'selected' : ''}>${s}</option>`).join('');
        
        const paySelect = document.getElementById('update-payment-select');
        if (paySelect) paySelect.innerHTML = ['PENDING', 'PAID'].map(s => `<option value="${s}" ${order.paymentStatus === s ? 'selected' : ''}>${s}</option>`).join('');

        const itemContainer = document.getElementById('modal-items');
        if (itemContainer) {
            itemContainer.innerHTML = order.items.map(item => `
                <div class="flex justify-between items-center p-3 bg-slate-50 rounded-xl">
                    <div><p class="font-bold text-slate-800 text-xs">${item.garmentName}</p><p class="text-[9px] text-slate-400 uppercase">${item.serviceType.replace('_',' ')} x ${item.quantity}</p></div>
                    <p class="font-black text-slate-900 text-xs">${sym}${(item.pricePerItem * item.quantity).toFixed(2)}</p>
                </div>
            `).join('');
        }

        const modal = document.getElementById('order-modal');
        if (modal) modal.classList.remove('hidden');
        lucide.createIcons();
    } catch (e) { console.error(e); }
}

async function handleStatusChange(val) {
    try {
        await apiFetch(`/orders/${currentOrderId}/status?status=${val}`, { method: 'PUT' });
        showToast('Workflow updated');
        refreshViews();
    } catch (e) { showToast(e.message, 'error'); }
}
async function handlePaymentChange(val) {
    try {
        await apiFetch(`/orders/${currentOrderId}/payment?status=${val}`, { method: 'PUT' });
        showToast('Payment updated');
        refreshViews();
    } catch (e) { showToast(e.message, 'error'); }
}
function refreshViews() { if (currentView === 'dashboard') loadDashboard(); if (currentView === 'orders') loadOrders(); }
function closeModal() { const m = document.getElementById('order-modal'); if (m) m.classList.add('hidden'); }

function showView(viewName) {
    const views = document.querySelectorAll('.view');
    views.forEach(v => v.classList.add('hidden'));
    const targetView = document.getElementById(`view-${viewName}`);
    if (targetView) targetView.classList.remove('hidden');
    
    const navBtns = document.querySelectorAll('nav button');
    navBtns.forEach(b => b.classList.remove('sidebar-active'));
    const activeBtn = document.getElementById(`nav-${viewName}`);
    if (activeBtn) activeBtn.classList.add('sidebar-active');
    
    currentView = viewName;
    if (viewName === 'dashboard') loadDashboard();
    if (viewName === 'orders') loadOrders();
    if (viewName === 'settings') loadLibrary();
    if (viewName === 'profile') loadProfile();
}

const STATUS_COLORS = {
    'RECEIVED': 'bg-blue-50 text-blue-600',
    'PROCESSING': 'bg-amber-50 text-amber-600',
    'READY': 'bg-emerald-50 text-emerald-600',
    'DELIVERED': 'bg-slate-100 text-slate-500'
};

function getStatusBadge(status) {
    const colors = STATUS_COLORS[status] || 'bg-slate-50 text-slate-500';
    return `<span class="px-3 py-1 ${colors} rounded-lg text-[10px] font-bold uppercase">${status}</span>`;
}

async function loadDashboard() {
    try {
        const data = await apiFetch('/dashboard');
        const sym = shopProfile.currencySymbol || '₹';
        
        const ready = document.getElementById('stat-ready');
        if (ready) ready.textContent = data.ordersByStatus.READY || 0;
        
        const totRev = document.getElementById('stat-total-revenue');
        if (totRev) totRev.textContent = `${sym}${data.totalRevenue.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
        
        const proc = document.getElementById('stat-processing');
        if (proc) proc.textContent = data.ordersByStatus.PROCESSING || 0;
        
        const prio = document.getElementById('stat-priority');
        if (prio) prio.textContent = data.totalPriorityOrders;

        renderRevenueChart(data.revenueByCategory);
        renderServiceChart(data.revenueByService);

        const custContainer = document.getElementById('top-customers-list');
        if (custContainer) {
            custContainer.innerHTML = data.topCustomers.length ? data.topCustomers.map(c => `
                <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full bg-white flex items-center justify-center font-bold text-blue-600 shadow-sm border border-slate-100">${c.customerName[0]}</div>
                        <div><p class="font-bold text-slate-800 text-sm">${c.customerName}</p><p class="text-[10px] font-bold text-slate-400">${c.totalOrders} Bookings</p></div>
                    </div>
                    <p class="font-black text-slate-900 text-sm">${sym}${c.totalSpent.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}</p>
                </div>
            `).join('') : '<p class="text-center text-slate-400 py-10">No customer data yet.</p>';
        }

        const orders = await apiFetch('/orders');
        const recentTable = document.getElementById('recent-orders-table');
        if (recentTable) {
            recentTable.innerHTML = orders.slice(0, 5).map(order => `
                <tr class="tr-hover cursor-pointer border-b border-slate-50" onclick="openOrderDetails('${order.orderId}')">
                    <td class="px-6 py-5">${getStatusBadge(order.status)}</td>
                    <td class="px-6 py-5">
                        <p class="font-bold text-slate-800 text-sm">${order.customerName}</p>
                        <p class="text-[10px] text-slate-400">#${order.orderId}</p>
                    </td>
                    <td class="px-6 py-5 text-xs font-medium text-slate-500">${order.items.length} items</td>
                    <td class="px-6 py-5 text-right font-black text-slate-900">${sym}${order.finalBill.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}</td>
                </tr>
            `).join('');
        }
        lucide.createIcons();
    } catch (e) { console.error(e); }
}

function renderRevenueChart(data) {
    const canvas = document.getElementById('revenueChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (revenueChart) revenueChart.destroy();
    
    revenueChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: Object.keys(data),
            datasets: [{
                label: 'Revenue',
                data: Object.values(data),
                backgroundColor: 'rgba(37, 99, 235, 0.8)',
                borderRadius: 12,
                barThickness: 30
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { display: false }, ticks: { display: false } },
                x: { grid: { display: false } }
            }
        }
    });
}

function renderServiceChart(data) {
    const canvas = document.getElementById('serviceChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (serviceChart) serviceChart.destroy();
    
    serviceChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(data).map(s => s.replace('_', ' ')),
            datasets: [{
                data: Object.values(data),
                backgroundColor: ['#6366f1', '#a855f7', '#ec4899'],
                borderWidth: 0,
                hoverOffset: 10
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: { 
                legend: { position: 'bottom', labels: { boxWidth: 8, font: { size: 10, weight: 'bold' }, padding: 20 } }
            },
            cutout: '70%'
        }
    });
}

async function loadOrders(query = '') {
    try {
        const orders = await apiFetch(`/orders${query ? `?query=${query}` : ''}`);
        const sym = shopProfile.currencySymbol || '₹';
        const table = document.getElementById('all-orders-table');
        if (table) {
            table.innerHTML = orders.map(order => `
                <tr class="tr-hover border-b border-slate-50">
                    <td class="p-6"><span class="font-black text-blue-600 text-sm">#${order.orderId}</span></td>
                    <td class="p-6"><p class="font-bold text-slate-800">${order.customerName}</p><p class="text-xs text-slate-400">${order.phoneNumber}</p></td>
                    <td class="p-6">${getStatusBadge(order.status)}</td>
                    <td class="p-6"><span class="px-3 py-1 ${order.paymentStatus === 'PAID' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'} rounded-lg text-[10px] font-bold">${order.paymentStatus}</span></td>
                    <td class="p-6 text-right font-black text-slate-900">${sym}${order.finalBill.toFixed(2)}</td>
                    <td class="p-6 text-center flex items-center justify-center gap-2">
                        <button onclick="openOrderDetails('${order.orderId}')" class="p-2 hover:bg-blue-50 rounded-lg text-blue-600 transition-colors"><i data-lucide="external-link" class="w-4 h-4"></i></button>
                        <button onclick="handleDeleteOrder('${order.orderId}')" class="p-2 hover:bg-rose-50 rounded-lg text-rose-400 hover:text-rose-600 transition-colors"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                    </td>
                </tr>
            `).join('');
            lucide.createIcons();
        }
    } catch (e) { console.error(e); }
}

async function handleDeleteOrder(orderId) {
    if (!confirm(`Delete order ${orderId}? This cannot be undone.`)) return;
    try {
        await apiFetch(`/orders/${orderId}`, { method: 'DELETE' });
        showToast('Order purged from repository');
        refreshViews();
    } catch (e) { showToast(e.message, 'error'); }
}

function debounceSearch() { clearTimeout(searchTimeout); searchTimeout = setTimeout(() => { loadOrders(document.getElementById('order-search').value); }, 300); }
