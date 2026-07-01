import React, { useState, useEffect } from 'react';
import { 
  Activity, 
  ShieldAlert, 
  CheckCircle, 
  AlertTriangle, 
  Send, 
  RefreshCw, 
  ToggleLeft, 
  ToggleRight, 
  Database, 
  Server,
  FileSpreadsheet
} from 'lucide-react';

interface WorkflowInstance {
  transactionId: string;       // processInstanceKey / transactionUUID
  accountId: string;           // target account Holder Name ID
  requestedAmount: number;     // requested loan value
  term: number;                // client-side loan duration
  activeStep: 'CREDIT_CHECK' | 'AUTO_DISBURSEMENT' | 'MANAGER_REVIEW' | 'COMPLETED';
  creditScore: number | null;
  eligible: boolean | null;
  kycStatus: 'APPROVED' | 'PENDING' | 'REJECTED' | 'UNKNOWN';
  status: 'APPROVED' | 'OPEN_TASK' | 'FRAUD_ALERT' | 'PROCESSING';
  timestamp: string;
}

interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

export default function App() {
  const [instances, setInstances] = useState<WorkflowInstance[]>([
    {
      transactionId: "multi-a8c9b2f2-1d54-47b1-b924-d2e858dbf4e5",
      accountId: "acc-98471",
      requestedAmount: 45000.00,
      term: 36,
      activeStep: 'COMPLETED',
      creditScore: 750,
      eligible: true,
      kycStatus: 'APPROVED',
      status: 'APPROVED',
      timestamp: new Date(Date.now() - 3600000).toLocaleTimeString()
    },
    {
      transactionId: "multi-f4b6d9e1-3c22-48a2-a881-c9e830eef711",
      accountId: "acc-10529",
      requestedAmount: 150000.00,
      term: 60,
      activeStep: 'MANAGER_REVIEW',
      creditScore: 600, // Fallback profile default score
      eligible: false,
      kycStatus: 'PENDING',
      status: 'OPEN_TASK',
      timestamp: new Date(Date.now() - 1800000).toLocaleTimeString()
    }
  ]);

  const [accountId, setAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [term, setTerm] = useState('36');
  
  const [errors, setErrors] = useState<{ accountId?: string; amount?: string; term?: string }>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Resilience4j HLD Status parameters
  const [isBureauDegraded, setIsBureauDegraded] = useState(false);
  const [circuitState, setCircuitState] = useState<'CLOSED' | 'OPEN - FAILOVER ACTIVE'>('CLOSED');
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  // Simulation steps for live workflows
  useEffect(() => {
    const interval = setInterval(() => {
      setInstances(prev => 
        prev.map(inst => {
          if (inst.activeStep === 'CREDIT_CHECK') {
            const isEligible = inst.creditScore !== null && inst.creditScore >= 700;
            const nextStep = isEligible ? 'COMPLETED' : 'MANAGER_REVIEW';
            const nextStatus = isEligible ? 'APPROVED' : 'OPEN_TASK';
            
            showToast('info', `Workflow ${inst.transactionId.substring(0,8)}: Bureau scoring evaluated.`);
            return {
              ...inst,
              activeStep: nextStep,
              status: nextStatus,
              eligible: isEligible
            };
          }
          return inst;
        })
      );
    }, 4000);

    return () => clearInterval(interval);
  }, []);

  const showToast = (type: 'success' | 'error' | 'warning' | 'info', message: string) => {
    const id = Math.random().toString(36).substring(2, 9);
    setToasts(prev => [...prev, { id, type, message }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4000);
  };

  const toggleBureauHealth = () => {
    const nextState = !isBureauDegraded;
    setIsBureauDegraded(nextState);
    if (nextState) {
      setCircuitState('OPEN - FAILOVER ACTIVE');
      showToast('warning', 'Resilience4j Alert: Downstream times exceeded limit. Circuit Breaker tripped!');
    } else {
      setCircuitState('CLOSED');
      showToast('success', 'Resilience4j Alert: Downstream healthy. Circuit Breaker CLOSED.');
    }
  };

  const validateForm = (): boolean => {
    const tempErrors: typeof errors = {};
    if (!accountId.trim()) tempErrors.accountId = 'Account ID is required';
    if (!amount) {
      tempErrors.amount = 'Requested amount is required';
    } else if (parseFloat(amount) <= 0) {
      tempErrors.amount = 'Amount must be greater than zero';
    }
    setErrors(tempErrors);
    return Object.keys(tempErrors).length === 0;
  };

  // Safe protective data parsing to handle numbers securely
  const parseBigDecimalSafely = (val: any): number => {
    if (val === null || val === undefined) return 0;
    const parsed = parseFloat(String(val).replace(/[^0-9.-]/g, ''));
    return isNaN(parsed) ? 0 : parsed;
  };

  // Fallback simulator for offline sandboxes
  const runLocalSimulation = (submittedAccountId: string, submittedAmount: number, submittedTerm: number) => {
    const mockUuid = "multi-sim-" + Math.random().toString(36).substring(2, 10);
    
    let creditScore = 730;
    let eligible = true;
    let kyc: WorkflowInstance['kycStatus'] = 'APPROVED';

    if (circuitState === 'OPEN - FAILOVER ACTIVE') {
      creditScore = 600; // conservative local scoring fallback
      eligible = false;
    } else {
      if (submittedAmount >= 80000) {
        creditScore = 620;
        eligible = false;
        kyc = 'PENDING';
      }
    }

    const newInstance: WorkflowInstance = {
      transactionId: mockUuid,
      accountId: submittedAccountId,
      requestedAmount: submittedAmount,
      term: submittedTerm,
      activeStep: 'CREDIT_CHECK',
      creditScore: creditScore,
      eligible: eligible,
      kycStatus: kyc,
      status: 'PROCESSING',
      timestamp: new Date().toLocaleTimeString()
    };

    setInstances(prev => [newInstance, ...prev]);
    showToast('info', `Failover Simulation: Running local validations for Instance ${mockUuid.substring(0,8)}`);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    const submittedAmount = parseBigDecimalSafely(amount);
    const submittedAccountId = accountId.trim();
    const submittedTerm = parseInt(term, 10);

    showToast('info', 'Connecting to Edge Gateway router...');

    try {
      if (submittedAccountId.toLowerCase() === 'poison-pill') {
        throw new Error("Serialization error: Poison pill detected");
      }

      // Query verification endpoints (using gateway port 8080)
      const response = await fetch('http://localhost:8080/api/v1/loans/apply', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          accountId: submittedAccountId,
          requestedAmount: submittedAmount
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: API rejected submission`);
      }

      const data = await response.json();
      const serverKey = data.processInstanceKey ? String(data.processInstanceKey) : "multi-instance-" + Math.random().toString(36).substring(2, 8);

      const newInstance: WorkflowInstance = {
        transactionId: serverKey,
        accountId: submittedAccountId,
        requestedAmount: submittedAmount,
        term: submittedTerm,
        activeStep: 'CREDIT_CHECK',
        creditScore: null,
        eligible: null,
        kycStatus: 'UNKNOWN',
        status: 'PROCESSING',
        timestamp: new Date().toLocaleTimeString()
      };

      setInstances(prev => [newInstance, ...prev]);
      showToast('success', `Gateway routing success. Process initiated: ${serverKey.substring(0, 8)}`);
      setAccountId('');
      setAmount('');
    } catch (err: any) {
      showToast('warning', `Gateway unreachable: ${err.message}. Initiating local TDD fallback.`);
      runLocalSimulation(submittedAccountId, submittedAmount, submittedTerm);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#070913] text-[#e2e8f0] p-6 flex flex-col font-sans select-none overflow-x-hidden">
      
      {/* Toast Manager */}
      <div className="fixed top-6 right-6 z-50 flex flex-col gap-3 w-96 pointer-events-none">
        {toasts.map(t => (
          <div 
            key={t.id} 
            className={`p-4 rounded-lg shadow-xl border flex items-start gap-3 pointer-events-auto transition-all ${
              t.type === 'success' ? 'bg-[#0a2316] border-[#10b981]/25 text-[#10b981]' :
              t.type === 'error' ? 'bg-[#2a1215] border-[#ef4444]/25 text-[#ef4444]' :
              t.type === 'warning' ? 'bg-[#2a1c0d] border-[#f59e0b]/25 text-[#f59e0b]' :
              'bg-[#0f1d33] border-[#3b82f6]/25 text-[#60a5fa]'
            }`}
          >
            {t.type === 'success' && <CheckCircle className="w-5 h-5 flex-shrink-0" />}
            {t.type === 'error' && <ShieldAlert className="w-5 h-5 flex-shrink-0" />}
            {t.type === 'warning' && <AlertTriangle className="w-5 h-5 flex-shrink-0" />}
            {t.type === 'info' && <Activity className="w-5 h-5 flex-shrink-0" />}
            <div>
              <p className="font-semibold text-xs uppercase font-mono">{t.type} feedback</p>
              <p className="text-xs mt-0.5 opacity-90">{t.message}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Header telemetry zone */}
      <header className="border-b border-slate-800 pb-4 mb-6 flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <div className="w-2.5 h-2.5 rounded-full bg-blue-500 animate-pulse" />
            <h1 className="text-lg font-bold tracking-wider text-white font-mono uppercase">FinStream // Control Tower v2</h1>
          </div>
          <p className="text-[10px] text-slate-400 mt-1 uppercase font-mono">Multi-Module Workspace Integration Console</p>
        </div>

        {/* Resilience4j Health Panel */}
        <div className="flex gap-4 items-center">
          <div className="flex items-center gap-6 px-4 py-2 bg-[#0c101d] border border-slate-800 rounded-md text-xs font-mono">
            <div className="flex items-center gap-2">
              <Server className="w-4 h-4 text-emerald-400" />
              <span>GATEWAY :8080</span>
            </div>
            
            {/* TRIP STATE INDICATOR BADGE */}
            <div className="flex items-center gap-2 border-l border-slate-800 pl-4">
              <Activity className="w-4 h-4 text-blue-400" />
              <span>CIRCUIT BREAKER: </span>
              <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                circuitState === 'CLOSED' ? 'bg-[#0b2b1a] text-emerald-400 border border-emerald-500/20' : 
                'bg-[#371317] text-red-400 border border-red-500/20 animate-pulse'
              }`}>
                {circuitState}
              </span>
            </div>
          </div>
          
          <div className="bg-slate-900 border border-slate-800 text-[10px] uppercase font-mono tracking-widest px-2.5 py-1 text-slate-400 rounded">
            Resilience4j Telemetry
          </div>
        </div>
      </header>

      {/* Main content grid */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 flex-grow">
        
        {/* Left Side: Loan Input Simulation */}
        <section className="bg-[#0c101d] border border-slate-850 rounded-lg p-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <Activity className="w-4 h-4 text-blue-400" />
              <h2 className="text-xs font-bold tracking-wider text-white font-mono uppercase">Application Injection</h2>
            </div>
            <p className="text-[11px] text-slate-400 mb-6 leading-relaxed">
              Inject transactions into the local microservice API gateway routes to run validation rules (50% balance thresholds and KYC).
            </p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-[10px] uppercase font-mono text-slate-400 mb-1">Account Holder ID</label>
                <input 
                  type="text" 
                  value={accountId}
                  onChange={(e) => setAccountId(e.target.value)}
                  placeholder="e.g. acc-98471"
                  className={`w-full bg-[#04060c] border rounded px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 ${
                    errors.accountId ? 'border-red-500 focus:ring-red-500' : 'border-slate-800 focus:ring-blue-500'
                  }`}
                />
                {errors.accountId && <p className="text-red-500 text-[9px] mt-1 font-mono">{errors.accountId}</p>}
              </div>

              <div>
                <label className="block text-[10px] uppercase font-mono text-slate-400 mb-1">Requested Capital (USD)</label>
                <div className="relative">
                  <span className="absolute left-3 top-2 text-xs text-slate-400 font-mono">$</span>
                  <input 
                    type="number" 
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    placeholder="0.00"
                    className={`w-full bg-[#04060c] border rounded pl-7 pr-3 py-2 text-xs text-white focus:outline-none focus:ring-1 ${
                      errors.amount ? 'border-red-500 focus:ring-red-500' : 'border-slate-800 focus:ring-blue-500'
                    }`}
                  />
                </div>
                {errors.amount && <p className="text-red-500 text-[9px] mt-1 font-mono">{errors.amount}</p>}
              </div>

              <div>
                <label className="block text-[10px] uppercase font-mono text-slate-400 mb-1">Amortization Term</label>
                <select 
                  value={term}
                  onChange={(e) => setTerm(e.target.value)}
                  className="w-full bg-[#04060c] border border-slate-800 rounded px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                >
                  <option value="12">12 Months (1 Year)</option>
                  <option value="24">24 Months (2 Years)</option>
                  <option value="36">36 Months (3 Years)</option>
                  <option value="60">60 Months (5 Years)</option>
                </select>
              </div>

              <button 
                type="submit" 
                disabled={isSubmitting}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white rounded py-2 text-xs uppercase font-bold font-mono flex items-center justify-center gap-2 transition-all cursor-pointer"
              >
                {isSubmitting ? (
                  <>
                    <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                    <span>Processing Gateway...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-3.5 h-3.5" />
                    <span>Inject Transaction</span>
                  </>
                )}
              </button>
            </form>
          </div>

          <div className="mt-8 p-3.5 bg-[#04060c] border border-slate-800 rounded-lg">
            <h3 className="text-[10px] font-bold text-white font-mono uppercase mb-2">Simulate Circuit Failover</h3>
            <p className="text-[10px] text-slate-400 mb-3 leading-relaxed">
              Degrade downstream Bureau API services. Trips the Resilience4j aspect state, applying failover defaults to all new applications.
            </p>
            <div className="flex items-center justify-between">
              <span className="text-[10px] uppercase font-mono text-slate-400">Bureau Health:</span>
              <button 
                onClick={toggleBureauHealth}
                className="flex items-center gap-1 focus:outline-none cursor-pointer"
              >
                {isBureauDegraded ? (
                  <ToggleRight className="w-8 h-8 text-red-500" />
                ) : (
                  <ToggleLeft className="w-8 h-8 text-slate-500" />
                )}
              </button>
            </div>
          </div>
        </section>

        {/* Right Side: Active Workflows Table */}
        <section className="xl:col-span-2 flex flex-col gap-6">
          
          {/* Health Stats */}
          <div className="bg-[#0c101d] border border-slate-850 rounded-lg p-5">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <FileSpreadsheet className="w-4 h-4 text-emerald-400" />
                <h2 className="text-xs font-bold tracking-wider text-white font-mono uppercase">Infrastructure Telemetry</h2>
              </div>
              <span className="text-[9px] text-slate-500 uppercase font-mono font-bold">Dynamic Stream</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              
              <div className="md:col-span-2 bg-[#04060c] border border-slate-850 rounded-lg p-4 flex flex-col justify-between">
                <div className="flex justify-between items-center">
                  <span className="text-[10px] uppercase font-mono text-slate-400">Circuit Breaker Properties</span>
                  <span className={`text-[9px] font-mono font-bold uppercase px-2 py-0.5 rounded ${
                    circuitState === 'CLOSED' ? 'bg-[#0f2d1d] text-[#10b981]' : 'bg-[#3b181a] text-[#ef4444]'
                  }`}>
                    {circuitState === 'CLOSED' ? 'CB ACTIVE' : 'CB TRIPPED'}
                  </span>
                </div>
                <div className="mt-2 text-xs font-bold font-mono text-white">
                  {circuitState === 'CLOSED' ? 'CLOSED - Downstream Credit Bureau REST queries fully active' : 'OPEN - Fallback Local Scoring (Default 600) Active'}
                </div>
              </div>

              <div className="bg-[#04060c] border border-slate-850 rounded-lg p-4 flex flex-col justify-between">
                <div className="flex justify-between items-center">
                  <span className="text-[10px] uppercase font-mono text-slate-400">Kafka DLT</span>
                  <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                </div>
                <div className="mt-2 text-xs font-bold font-mono text-white">
                  loan-audit-stream.DLT
                </div>
              </div>

            </div>
          </div>

          {/* Active Workflows Table */}
          <div className="bg-[#0c101d] border border-slate-850 rounded-lg p-5 flex-grow flex flex-col">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Database className="w-4 h-4 text-blue-400" />
                <h2 className="text-xs font-bold tracking-wider text-white font-mono uppercase">Control Tower Asset Table</h2>
              </div>
              <span className="text-[10px] px-2 py-0.5 bg-[#04060c] border border-slate-800 text-slate-400 rounded font-mono">
                Active Count: {instances.length}
              </span>
            </div>

            <div className="overflow-x-auto flex-grow">
              <table className="w-full text-left text-[11px] font-mono border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase text-[9px] bg-[#090d16]">
                    <th className="py-2.5 px-3 font-bold">Transaction / App ID</th>
                    <th className="py-2.5 px-3 font-bold">Account ID</th>
                    <th className="py-2.5 px-3 font-bold text-center">KYC</th>
                    <th className="py-2.5 px-3 font-bold text-right">Requested Capital</th>
                    <th className="py-2.5 px-3 font-bold text-center">Credit Rating</th>
                    <th className="py-2.5 px-3 font-bold text-center">Active Step</th>
                    <th className="py-2.5 px-3 font-bold text-right">State Decision</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/40">
                  {instances.map((inst) => (
                    <tr key={inst.transactionId} className="hover:bg-slate-800/20 transition-colors">
                      <td className="py-3 px-3 font-bold text-slate-300">
                        {inst.transactionId.substring(0, 12)}...
                      </td>
                      <td className="py-3 px-3 text-slate-400">
                        {inst.accountId}
                      </td>
                      <td className="py-3 px-3 text-center">
                        <span className={`text-[9px] px-2 py-0.5 rounded font-bold ${
                          inst.kycStatus === 'APPROVED' ? 'bg-emerald-950/40 text-emerald-400' :
                          inst.kycStatus === 'PENDING' ? 'bg-amber-950/40 text-amber-400' :
                          inst.kycStatus === 'REJECTED' ? 'bg-red-950/40 text-red-400' :
                          'bg-slate-900 text-slate-400'
                        }`}>
                          {inst.kycStatus}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right font-bold text-white">
                        ${inst.requestedAmount.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                      </td>
                      <td className="py-3 px-3 text-center font-bold text-slate-300">
                        {inst.creditScore !== null ? inst.creditScore : 'EVALUATING'}
                      </td>
                      <td className="py-3 px-3 text-center">
                        <span className={`text-[9px] px-2 py-0.5 rounded font-bold uppercase ${
                          inst.activeStep === 'CREDIT_CHECK' ? 'bg-blue-900/40 text-blue-400' :
                          inst.activeStep === 'AUTO_DISBURSEMENT' ? 'bg-emerald-900/40 text-emerald-400' :
                          inst.activeStep === 'MANAGER_REVIEW' ? 'bg-amber-900/40 text-amber-400' :
                          'bg-slate-900 text-slate-400'
                        }`}>
                          {inst.activeStep.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right">
                        <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-[9px] font-bold uppercase ${
                          inst.status === 'APPROVED' ? 'bg-emerald-950 text-emerald-400 border border-emerald-500/20' :
                          inst.status === 'OPEN_TASK' ? 'bg-amber-950 text-amber-400 border border-amber-500/20' :
                          inst.status === 'FRAUD_ALERT' ? 'bg-red-950 text-red-400 border border-red-500/20' :
                          'bg-blue-950 text-blue-400 border border-blue-500/20 animate-pulse'
                        }`}>
                          <span className={`w-1.5 h-1.5 rounded-full ${
                            inst.status === 'APPROVED' ? 'bg-emerald-400' :
                            inst.status === 'OPEN_TASK' ? 'bg-amber-400' :
                            inst.status === 'FRAUD_ALERT' ? 'bg-red-400' :
                            'bg-blue-400'
                          }`} />
                          {inst.status === 'APPROVED' && 'APPROVED'}
                          {inst.status === 'OPEN_TASK' && 'MANAGER REVIEW'}
                          {inst.status === 'FRAUD_ALERT' && 'FRAUD ALERT'}
                          {inst.status === 'PROCESSING' && 'PROCESSING'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>

      </div>

      <footer className="border-t border-slate-800 mt-6 pt-4 flex flex-wrap items-center justify-between text-[10px] text-slate-500 font-mono">
        <span>FINSTREAM INTEGRATION CONSOLE V2</span>
        <div className="flex gap-4">
          <span>HOST CLUSTER: ONLINE</span>
          <span>DATE: {new Date().toLocaleDateString()}</span>
        </div>
      </footer>

    </div>
  );
}
