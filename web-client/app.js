// Weather System - Main JavaScript
// API Endpoints
const WEATHER_API = 'http://localhost:8085/api/weather';
const ALERTS_API = 'http://localhost:8084/api/alert-rules';
const REPORT_API = 'http://localhost:8085/api/report';
const XMLRPC_API = 'http://localhost:8085/api/xmlrpc-proxy';
const STATS_API = 'http://localhost:8089/api/statistics';

// Global state
let alertsWithLinks = {};

// ============================================
// WEATHER - gRPC
// ============================================

async function getWeather() {
    const city = document.getElementById('city').value.trim();
    if (!city) {
        showToast('Enter a city name', 'error');
        return;
    }

    const loading = document.getElementById('loading');
    const result = document.getElementById('result');

    loading.style.display = 'block';
    result.classList.add('hidden');

    try {
        const response = await fetch(`${WEATHER_API}/${city}`);
        const data = await response.json();

        // Fetch alerts to find triggered one
        let triggeredAlert = null;
        if (data.status === 'ALERT') {
            try {
                const alertsResponse = await fetch(`${ALERTS_API}?page=0&size=100`);
                const alertsData = await alertsResponse.json();
                const activeAlerts = alertsData.content.filter(a => a.active && a.alertType === 'TEMPERATURE');

                for (let alert of activeAlerts) {
                    if (eval(`${data.temperature} ${alert.operator} ${alert.threshold}`)) {
                        triggeredAlert = alert;
                        break;
                    }
                }
            } catch (e) {
                console.error('Failed to fetch alerts:', e);
            }
        }

        result.innerHTML = `
            <div class="weather-display">
                <div style="text-align: center;">
                    <div class="weather-temp">${data.temperature.toFixed(1)}°C</div>
                    <div class="weather-description">${data.description}</div>
                    <div style="margin-top: 16px;">
                        <span class="weather-status ${data.status === 'ALERT' ? 'alert' : 'normal'}">
                            ${data.status}
                        </span>
                    </div>
                    <div style="margin-top: 20px; padding-top: 20px; border-top: 2px solid rgba(24, 144, 255, 0.2);">
                        <div style="color: #718096; font-size: 14px; margin-bottom: 8px;">Location</div>
                        <div style="color: #2d3748; font-size: 18px; font-weight: 600;">${data.city}</div>
                    </div>
                    ${triggeredAlert ? `
                        <div style="margin-top: 24px; padding: 20px; background: #fff7e6; border: 2px solid ${triggeredAlert.color}; border-radius: 12px; text-align: left;">
                            <div style="font-weight: 700; font-size: 14px; color: ${triggeredAlert.color}; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px;">
                                ${triggeredAlert.severity} Alert
                            </div>
                            <div style="color: #2d3748; font-size: 15px; line-height: 1.5; margin-bottom: 8px;">
                                ${triggeredAlert.message}
                            </div>
                            <div style="color: #718096; font-size: 13px;">
                                ${triggeredAlert.alertType} ${triggeredAlert.operator} ${triggeredAlert.threshold}°C
                            </div>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
        result.classList.remove('hidden');
        showToast('Weather data loaded successfully!');
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    } finally {
        loading.style.display = 'none';
    }
}

// ============================================
// STREAMING - gRPC Server Streaming with SSE
// ============================================

let streamInProgress = false;
let currentEventSource = null;

async function startWeatherStream() {
    if (streamInProgress) {
        showToast('Stream already in progress!', 'error');
        return;
    }

    const city = document.getElementById('streamCity').value.trim();
    const updates = document.getElementById('streamUpdates').value;
    const interval = document.getElementById('streamInterval').value;

    if (!city) {
        showToast('Enter a city name', 'error');
        return;
    }

    const statusDiv = document.getElementById('streamStatus');
    const resultDiv = document.getElementById('streamResult');
    const button = document.getElementById('streamButton');

    streamInProgress = true;
    button.disabled = true;
    button.style.opacity = '0.5';
    button.textContent = 'Streaming...';

    statusDiv.style.display = 'block';
    statusDiv.style.background = '#e6f7ff';
    statusDiv.style.border = '2px solid #91d5ff';
    statusDiv.style.borderRadius = '12px';
    statusDiv.style.padding = '20px';
    statusDiv.style.color = '#2d3748';
    statusDiv.innerHTML = `
        <div style="font-weight: 700; font-size: 16px; margin-bottom: 8px; color: #1890ff;">
            Live Weather Stream Started
        </div>
        <div style="font-size: 14px; color: #4a5568;">
            City: <strong>${city}</strong> | Updates: <strong>${updates}</strong> | Interval: <strong>${interval}s</strong>
        </div>
        <div style="margin-top: 12px; font-size: 13px; color: #718096;">
            Fetching updates from OpenWeatherMap API in real-time...
        </div>
    `;

    resultDiv.innerHTML = `
        <table style="margin-top: 20px;">
            <thead>
                <tr>
                    <th>#</th>
                    <th>City</th>
                    <th>Temperature</th>
                    <th>Description</th>
                    <th>Time</th>
                </tr>
            </thead>
            <tbody id="streamTableBody">
                <tr>
                    <td colspan="5" style="text-align: center; color: #64748b;">
                        Waiting for first update...
                    </td>
                </tr>
            </tbody>
        </table>
    `;
    resultDiv.classList.remove('hidden');

    const startTime = Date.now();
    let updateCount = 0;

    const eventSource = new EventSource(
        `${WEATHER_API}/stream-sse/${city}?updates=${updates}&intervalSeconds=${interval}`
    );
    currentEventSource = eventSource;

    eventSource.addEventListener('weather-update', (event) => {
        const update = JSON.parse(event.data);
        updateCount++;

        console.log('Received update:', update);
        addStreamUpdateRow(update);

        const elapsed = ((Date.now() - startTime) / 1000).toFixed(1);
        statusDiv.innerHTML = `
            <div style="font-weight: 700; font-size: 16px; margin-bottom: 8px; color: #1890ff;">
                Streaming... (${updateCount}/${updates})
            </div>
            <div style="font-size: 14px; color: #4a5568;">
                City: <strong>${city}</strong> | Elapsed: <strong>${elapsed}s</strong>
            </div>
            <div style="margin-top: 12px; font-size: 13px; color: #718096; animation: pulse 2s infinite;">
                Receiving live updates...
            </div>
        `;
    });

    eventSource.addEventListener('error', (event) => {
        console.log('Stream ended or error occurred');
        eventSource.close();
        currentEventSource = null;

        const elapsed = ((Date.now() - startTime) / 1000).toFixed(1);

        statusDiv.style.background = '#f6ffed';
        statusDiv.style.border = '2px solid #b7eb8f';
        statusDiv.innerHTML = `
            <div style="font-weight: 700; font-size: 16px; margin-bottom: 8px; color: #52c41a;">
                Stream Completed Successfully!
            </div>
            <div style="font-size: 14px; color: #4a5568;">
                Received <strong>${updateCount}</strong> updates in <strong>${elapsed}s</strong>
            </div>
        `;

        showToast(`Stream completed! ${updateCount} updates received`);

        streamInProgress = false;
        button.disabled = false;
        button.style.opacity = '1';
        button.textContent = '🚀 Start Live Stream';
    });
}

function addStreamUpdateRow(update) {
    const tbody = document.getElementById('streamTableBody');

    if (tbody.querySelector('td[colspan="5"]')) {
        tbody.innerHTML = '';
    }

    const row = document.createElement('tr');
    row.innerHTML = `
        <td style="font-weight: 700; color: #3b82f6;">#${update.updateNumber}</td>
        <td>${update.city}</td>
        <td style="font-weight: 700; color: #10b981;">${update.temperature.toFixed(1)}°C</td>
        <td>${update.description}</td>
        <td style="color: #64748b; font-size: 13px;">${new Date(update.timestamp).toLocaleTimeString()}</td>
    `;

    tbody.appendChild(row);
    row.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// ============================================
// ALERTS CRUD with HATEOAS
// ============================================

async function loadAlerts() {
    try {
        const response = await fetch(`${ALERTS_API}?page=0&size=20&sortBy=createdAt`);
        const data = await response.json();

        alertsWithLinks = {};
        data.content.forEach(alert => {
            if (alert._links) {
                alertsWithLinks[alert.id] = alert._links;
            } else if (alert.links && Array.isArray(alert.links)) {
                const linksObj = {};
                alert.links.forEach(link => {
                    linksObj[link.rel] = { href: link.href };
                });
                alertsWithLinks[alert.id] = linksObj;
            }
        });

        console.log('HATEOAS links loaded:', alertsWithLinks);
        displayAlerts(data.content);
    } catch (error) {
        showToast('Failed to load alerts', 'error');
    }
}

function displayAlerts(alerts) {
    const tbody = document.getElementById('alertsTable');

    if (!alerts || alerts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No alerts configured</td></tr>';
        return;
    }

    tbody.innerHTML = alerts.map(alert => `
        <tr id="row-${alert.id}">
            <td>${alert.id}</td>
            <td>${alert.alertType}</td>
            <td>${alert.operator} ${alert.threshold}</td>
            <td><span class="alert-badge" style="background: ${getSeverityColor(alert.severity)};">${alert.severity}</span></td>
            <td><div class="color-preview" style="background: ${alert.color};"></div></td>
            <td>${alert.message}</td>
            <td>
                <button class="btn-warning" onclick="toggleAlert(${alert.id})">${alert.active ? 'Deactivate' : 'Activate'}</button>
                <button class="btn-edit" onclick="editAlert(${alert.id})">Edit</button>
                <button class="btn-danger" onclick="deleteAlert(${alert.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

function getSeverityColor(severity) {
    const colors = {'LOW': '#48bb78', 'MEDIUM': '#ed8936', 'HIGH': '#f56565', 'EXTREME': '#9b2c2c'};
    return colors[severity] || '#718096';
}

async function addAlert(event) {
    event.preventDefault();
    const data = {
        alertType: document.getElementById('alertType').value,
        threshold: parseFloat(document.getElementById('threshold').value),
        operator: document.getElementById('operator').value,
        severity: document.getElementById('severity').value,
        color: document.getElementById('color').value,
        message: document.getElementById('message').value,
        active: true
    };
    try {
        const response = await fetch(ALERTS_API, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        if (response.ok) {
            showToast('Alert added!');
            document.getElementById('alertForm').reset();
            loadAlerts();
        } else {
            showToast('Failed to add alert', 'error');
        }
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

function editAlert(id) {
    const row = document.getElementById(`row-${id}`);
    const cells = row.cells;
    document.getElementById('editId').value = id;
    document.getElementById('editAlertType').value = cells[1].textContent;
    const condition = cells[2].textContent.trim().split(' ');
    document.getElementById('editOperator').value = condition[0];
    document.getElementById('editThreshold').value = parseFloat(condition[1]);
    document.getElementById('editSeverity').value = cells[3].textContent.trim();
    document.getElementById('editColor').value = cells[4].querySelector('.color-preview').style.background;
    document.getElementById('editMessage').value = cells[5].textContent;
    document.getElementById('editModal').classList.add('show');
}

function closeEditModal() {
    document.getElementById('editModal').classList.remove('show');
}

async function saveEdit(event) {
    event.preventDefault();
    const id = document.getElementById('editId').value;

    const links = alertsWithLinks[id];
    const updateUrl = links && links.update ? links.update.href : `${ALERTS_API}/${id}`;

    const data = {
        alertType: document.getElementById('editAlertType').value,
        threshold: parseFloat(document.getElementById('editThreshold').value),
        operator: document.getElementById('editOperator').value,
        severity: document.getElementById('editSeverity').value,
        color: document.getElementById('editColor').value,
        message: document.getElementById('editMessage').value,
        active: true
    };

    try {
        const response = await fetch(updateUrl, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        if (response.ok) {
            const useHateoas = links && links.update ? ' using HATEOAS!' : '';
            showToast('Alert updated' + useHateoas);
            closeEditModal();
            loadAlerts();
        }
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

async function deleteAlert(id) {
    if (!confirm('Delete this alert?')) return;

    const links = alertsWithLinks[id];
    const deleteUrl = links && links.delete ? links.delete.href : `${ALERTS_API}/${id}`;

    try {
        const response = await fetch(deleteUrl, {method: 'DELETE'});
        if (response.ok) {
            const useHateoas = links && links.delete ? ' using HATEOAS!' : '';
            showToast('Alert deleted' + useHateoas);
            loadAlerts();
        }
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

async function toggleAlert(id) {
    const links = alertsWithLinks[id];
    const toggleUrl = links && links.toggle ? links.toggle.href : `${ALERTS_API}/${id}/toggle`;

    try {
        const response = await fetch(toggleUrl, {method: 'PATCH'});
        if (response.ok) {
            const useHateoas = links && links.toggle ? ' using HATEOAS!' : '';
            showToast('Alert toggled' + useHateoas);
            loadAlerts();
        }
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

// ============================================
// SOAP REPORT
// ============================================

async function generateReport() {
    const city = document.getElementById('reportCity').value.trim();
    const days = document.getElementById('reportDays').value;

    if (!city) {
        showToast('Enter a city name', 'error');
        return;
    }

    const loading = document.getElementById('reportLoading');
    const result = document.getElementById('reportResult');

    loading.classList.remove('hidden');
    result.classList.add('hidden');

    try {
        const response = await fetch(`${REPORT_API}/${city}?days=${days}`);
        const data = await response.json();

        displayReport(data);
        showToast('SOAP report generated!');
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    } finally {
        loading.classList.add('hidden');
    }
}

function displayReport(data) {
    const resultDiv = document.getElementById('reportResult');

    if (!data || !data.city) {
        resultDiv.innerHTML = '<div class="empty-state">No data available</div>';
        resultDiv.classList.remove('hidden');
        return;
    }

    resultDiv.innerHTML = `
        <div class="report-grid">
            <div class="report-item">
                <div class="label">City</div>
                <div class="value">${data.city}</div>
            </div>
            <div class="report-item">
                <div class="label">Average Temp</div>
                <div class="value">${parseFloat(data.avgTemp || data.averageTemperature || 0).toFixed(1)}°C</div>
            </div>
            <div class="report-item">
                <div class="label">Min Temp</div>
                <div class="value">${parseFloat(data.minTemp || data.minTemperature || 0).toFixed(1)}°C</div>
            </div>
            <div class="report-item">
                <div class="label">Max Temp</div>
                <div class="value">${parseFloat(data.maxTemp || data.maxTemperature || 0).toFixed(1)}°C</div>
            </div>
            <div class="report-item">
                <div class="label">Report Date</div>
                <div class="value">${data.reportDate || data.generatedAt || 'N/A'}</div>
            </div>
        </div>
        ${data.summary ? `
            <div style="margin-top: 24px; padding: 20px; background: #e6f7ff; border: 2px solid #91d5ff; border-radius: 12px;">
                <div style="font-weight: 700; margin-bottom: 10px; color: #1890ff; font-size: 15px;">Summary</div>
                <div style="color: #4a5568; line-height: 1.6; font-size: 14px;">${data.summary}</div>
            </div>
        ` : ''}
    `;

    resultDiv.classList.remove('hidden');
}

// ============================================
// XML-RPC HISTORICAL DATA
// ============================================

async function getXmlRpcHistoryNew() {
    const city = document.getElementById('xmlrpcCityNew').value.trim();
    const days = document.getElementById('xmlrpcDaysNew').value;

    if (!city) {
        showToast('Enter a city name', 'error');
        return;
    }

    const loading = document.getElementById('xmlrpcLoadingNew');
    loading.classList.remove('hidden');

    try {
        const response = await fetch(`${XMLRPC_API}/history/${city}?days=${days}`);
        const data = await response.json();
        displayXmlRpcHistoryNew(data, false);
        showToast('XML-RPC history loaded!');
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    } finally {
        loading.classList.add('hidden');
    }
}

async function getXmlRpcHistoryAsyncNew() {
    const city = document.getElementById('xmlrpcCityNew').value.trim();
    const days = document.getElementById('xmlrpcDaysNew').value;

    if (!city) {
        showToast('Enter a city name', 'error');
        return;
    }

    const loading = document.getElementById('xmlrpcLoadingNew');
    loading.classList.remove('hidden');

    const startTime = Date.now();

    try {
        const response = await fetch(`${XMLRPC_API}/history-async/${city}?days=${days}`);
        const data = await response.json();

        const elapsed = ((Date.now() - startTime) / 1000).toFixed(1);
        displayXmlRpcHistoryNew(data, true, elapsed);
        showToast('XML-RPC async history loaded!');
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    } finally {
        loading.classList.add('hidden');
    }
}

async function getXmlRpcStatisticsNew() {
    const city = document.getElementById('xmlrpcCityNew').value.trim();

    if (!city) {
        showToast('Enter a city name', 'error');
        return;
    }

    const loading = document.getElementById('xmlrpcLoadingNew');
    loading.classList.remove('hidden');

    try {
        const response = await fetch(`${XMLRPC_API}/statistics/${city}`);
        const data = await response.json();

        const resultDiv = document.getElementById('xmlrpcResultNew');
        resultDiv.innerHTML = `
            <div class="report-grid">
                <div class="report-item">
                    <div class="label">City</div>
                    <div class="value">${data.city}</div>
                </div>
                <div class="report-item">
                    <div class="label">Average Temp</div>
                    <div class="value">${data.avgTemp ? data.avgTemp.toFixed(1) : 'N/A'}°C</div>
                </div>
                <div class="report-item">
                    <div class="label">Min Temp</div>
                    <div class="value">${data.minTemp ? data.minTemp.toFixed(1) : 'N/A'}°C</div>
                </div>
                <div class="report-item">
                    <div class="label">Max Temp</div>
                    <div class="value">${data.maxTemp ? data.maxTemp.toFixed(1) : 'N/A'}°C</div>
                </div>
                <div class="report-item">
                    <div class="label">Records</div>
                    <div class="value">${data.recordCount || 0}</div>
                </div>
            </div>
        `;
        resultDiv.classList.remove('hidden');
        showToast('XML-RPC statistics loaded!');
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    } finally {
        loading.classList.add('hidden');
    }
}

function displayXmlRpcHistoryNew(data, isAsync, elapsed) {
    const resultDiv = document.getElementById('xmlrpcResultNew');

    if (!Array.isArray(data) || data.length === 0) {
        resultDiv.innerHTML = '<div class="empty-state">No historical data available. Check weather first!</div>';
        resultDiv.classList.remove('hidden');
        return;
    }

    if (data[0].error) {
        resultDiv.innerHTML = `
            <div style="padding: 24px; background: #fff1f0; border: 2px solid #ffa39e; border-radius: 12px; text-align: center;">
                <div style="font-size: 18px; font-weight: 700; margin-bottom: 8px; color: #cf1322;">
                    No Historical Data
                </div>
                <div style="font-size: 14px; color: #4a5568;">
                    ${data[0].error}
                </div>
                <div style="margin-top: 12px; font-size: 13px; color: #718096;">
                    Tip: First get current weather for ${data[0].city} to generate history!
                </div>
            </div>
        `;
        resultDiv.classList.remove('hidden');
        return;
    }

    let html = `
        <div style="padding: 24px; background: #e6f7ff; border: 2px solid #91d5ff; border-radius: 12px; margin-bottom: 20px;">
            <div style="font-size: 18px; font-weight: 700; margin-bottom: 8px; color: #1890ff;">
                ${isAsync ? 'Asynchronous Call' : 'Synchronous Call'}
            </div>
            <div style="font-size: 14px; color: #4a5568;">
                Retrieved <strong>${data.length}</strong> records from Weather Provider via XML-RPC protocol
                ${isAsync ? ` • Completed in <strong>${elapsed}s</strong> (includes 3s server delay)` : ''}
            </div>
        </div>
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>City</th>
                    <th>Temperature</th>
                    <th>Description</th>
                    <th>Timestamp</th>
                </tr>
            </thead>
            <tbody>
    `;

    data.forEach((record, index) => {
        html += `
            <tr>
                <td>${index + 1}</td>
                <td>${record.city || 'Unknown'}</td>
                <td style="font-weight: 700; color: #3b82f6;">${record.temperature ? record.temperature.toFixed(1) : 'N/A'}°C</td>
                <td>${record.description || 'N/A'}</td>
                <td style="color: #64748b; font-size: 14px;">${record.timestamp || record.date || 'N/A'}</td>
            </tr>
        `;
    });

    html += `
            </tbody>
        </table>
    `;

    resultDiv.innerHTML = html;
    resultDiv.classList.remove('hidden');
}

// ============================================
// ADVANCED STATISTICS (Spring Cloud Feign)
// ============================================

async function getAdvancedStatistics() {
    const city = document.getElementById('statsCity').value.trim();
    if (!city) {
        showToast('Enter a city name', 'error');
        return;
    }

    const loading = document.getElementById('statsLoading');
    const result = document.getElementById('statsResult');

    loading.classList.remove('hidden');
    result.classList.add('hidden');

    try {
        const response = await fetch(`${STATS_API}/${city}`);
        const data = await response.json();

        if (data.error) {
            result.innerHTML = `
                <div style="padding: 24px; background: #fff1f0; border: 2px solid #ffa39e; border-radius: 12px; text-align: center;">
                    <div style="font-size: 18px; font-weight: 700; margin-bottom: 8px; color: #cf1322;">Warning: ${data.error}</div>
                    <div style="font-size: 14px; color: #4a5568;">${data.hint || 'Generate weather data first!'}</div>
                </div>
            `;
        } else {
            result.innerHTML = `
                <div style="padding: 24px; background: #f6ffed; border: 2px solid #b7eb8f; border-radius: 12px; margin-bottom: 20px;">
                    <div style="font-size: 18px; font-weight: 700; margin-bottom: 8px; color: #52c41a;">
                        Advanced Statistics for ${data.city}
                    </div>
                    <div style="font-size: 14px; color: #4a5568;">
                        Calculated using Spring Cloud Feign Client • <strong>${data.recordCount}</strong> measurements
                    </div>
                </div>

                <div class="report-grid">
                    <div class="report-item">
                        <div class="label">Average</div>
                        <div class="value">${data.average.toFixed(1)}°C</div>
                    </div>
                    <div class="report-item">
                        <div class="label">Median</div>
                        <div class="value" style="color: #8b5cf6;">${data.median.toFixed(1)}°C</div>
                    </div>
                    <div class="report-item">
                        <div class="label">Min / Max</div>
                        <div class="value">${data.min.toFixed(1)}°C / ${data.max.toFixed(1)}°C</div>
                    </div>
                    <div class="report-item">
                        <div class="label">Std. Deviation</div>
                        <div class="value" style="color: #f59e0b;">${data.standardDeviation.toFixed(1)}°C</div>
                    </div>
                    <div class="report-item">
                        <div class="label">Trend</div>
                        <div class="value" style="color: ${data.trend === 'RISING' ? '#f56565' : data.trend === 'FALLING' ? '#4299e1' : '#718096'};">
                            ${data.trend === 'RISING' ? '↗ Rising' : data.trend === 'FALLING' ? '↘ Falling' : '→ Stable'}
                        </div>
                    </div>
                    <div class="report-item">
                        <div class="label">Outliers</div>
                        <div class="value">${data.outliers.length}</div>
                    </div>
                </div>

                ${data.outliers.length > 0 ? `
                    <div style="margin-top: 20px; padding: 20px; background: #fffbe6; border: 2px solid #ffe58f; border-radius: 12px;">
                        <div style="font-weight: 700; margin-bottom: 10px; color: #d48806; font-size: 15px;">Warning: Anomalies Detected (>2σ)</div>
                        <div style="color: #4a5568; font-size: 14px;">${data.outliers.map(val => val.toFixed(1) + '°C').join(', ')}</div>
                    </div>
                ` : ''}

                <div style="margin-top: 20px; padding: 20px; background: #e6f7ff; border: 2px solid #91d5ff; border-radius: 12px;">
                    <div style="font-weight: 700; margin-bottom: 10px; color: #1890ff; font-size: 15px;">📊 Statistical Analysis</div>
                    <div style="color: #4a5568; font-size: 14px;">
                        Based on <strong>${data.recordCount}</strong> measurements. 
                        ${data.trend === 'RISING' ? 'Temperature shows an upward trend.' : 
                          data.trend === 'FALLING' ? 'Temperature shows a downward trend.' : 
                          'Temperature remains stable.'}
                        ${data.standardDeviation < 1 ? ' Low variability.' : ' High variability.'}
                    </div>
                </div>
            `;
            showToast('Statistics calculated successfully!');
        }

        result.classList.remove('hidden');

    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    } finally {
        loading.classList.add('hidden');
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = 'toast' + (type === 'error' ? ' error' : '');
    toast.style.display = 'block';
    setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    loadAlerts();
});

