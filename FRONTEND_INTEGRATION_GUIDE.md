# Frontend Integration Guide: Party Commodity Mapping & Registration Workflow

This guide details the exact changes needed in the frontend repository (on your other machine) to align with the backend's updated commodity mapping and party registration workflow.

---

## 1. Summary of What Changed in the Backend

1. **DTOs Updated**:
   * `AddPartyRequest` now accepts:
     * `commodities`: `Set<String>` (e.g. `["Soybean", "Wheat"]`)
     * `commodityIds`: `Set<Long>` (optional alternative)
     * `status`: `String` (e.g. `"PENDING_APPROVAL"`, `"ACTIVE"`)
     * `registrationType`: `String` (e.g. `"SELF_REGISTERED"`, `"ADMIN_CREATED"`)
   * `PartyDTO` now returns:
     * `commodities`: `Set<String>` (list of mapped commodity names)
2. **Database Persistence**:
   * Saves records into the `party_commodity_mapping` table automatically upon party creation.
   * Resolves commodity names against the Commodity Master.
3. **Notification Workflow**:
   * **Self-Registration (`PENDING_APPROVAL`)**:
     * Does **not** send login credentials to the party immediately.
     * Dispatches an alert directly to the **Admin's WhatsApp** with party details, location, and mapped commodities asking them to review and approve.
   * **Admin Approval (`PATCH /api/v1/parties/{id}/activate`)**:
     * Triggers the WhatsApp activation template with login credentials to **all registered contact numbers** of the party.
   * **Admin Direct Add (`ACTIVE`)**:
     * Triggers the WhatsApp activation message immediately.

---

## 2. Frontend Files to Update

### File: `src/services/parties/partyService.ts`

#### Step A: Include `commodities` and `status` in the `createParty` API payload

Find `createParty` around line 201:

```typescript
// BEFORE:
const payload = {
  companyName: partyData.name,
  gstin: partyData.gstin || partyData.gstNumber,
  pan: partyData.pan,
  contactPerson: partyData.contactPerson,
  email: partyData.email?.trim() || undefined,
  phone: partyData.phone || partyData.whatsappNumber,
  alternatePhone: partyData.alternatePhone,
  address: partyData.address,
  city: partyData.city,
  taluka: partyData.taluka,
  plantLocation: partyData.plantLocation || partyData.plantDetails,
  state: partyData.state,
  country: partyData.country || 'India',
  postalCode: partyData.pincode,
  description: partyData.adminRemarks,
  website: partyData.website,
  roleIds
};
```

**Change it to include `commodities`, `status`, and `registrationType`:**

```typescript
// AFTER:
const payload = {
  companyName: partyData.name,
  gstin: partyData.gstin || partyData.gstNumber,
  pan: partyData.pan,
  contactPerson: partyData.contactPerson,
  email: partyData.email?.trim() || undefined,
  phone: partyData.phone || partyData.whatsappNumber,
  alternatePhone: partyData.alternatePhone,
  address: partyData.address,
  city: partyData.city,
  taluka: partyData.taluka,
  plantLocation: partyData.plantLocation || partyData.plantDetails,
  state: partyData.state,
  country: partyData.country || 'India',
  postalCode: partyData.pincode,
  description: partyData.adminRemarks,
  website: partyData.website,
  roleIds,
  status: partyData.status || 'ACTIVE',
  registrationType: partyData.status === 'PENDING_APPROVAL' ? 'SELF_REGISTERED' : 'ADMIN_CREATED',
  commodities: partyData.commodities || []
};
```

---

#### Step B: Populate `commodities` from Backend DTO in `mapPartyDtoToParty`

Find `mapPartyDtoToParty` around line 79:

```typescript
// BEFORE:
commodities: dto.commodities || [],
```

Ensure it maps `dto.commodities` directly:
```typescript
commodities: Array.isArray(dto.commodities) ? dto.commodities : (dto.commodities ? Array.from(dto.commodities) : []),
```

---

#### Step C: Adjust Frontend WhatsApp Trigger in `createParty`

In `partyService.ts`, when `createdParty.status === 'PENDING_APPROVAL'`, the backend **already sends an alert to Admin**, and the party should **not** receive a "You have been registered with password" message until approved.

Find lines 319-338 in `createParty`:

```typescript
// BEFORE:
notificationService.sendWhatsAppNotification(
  createdParty.id,
  createdParty.name,
  undefined,
  undefined,
  'ACCOUNT_ACTIVATION',
  ...
);
```

**Wrap with a condition so it only fires if ACTIVE:**

```typescript
// AFTER:
if (createdParty.status === 'ACTIVE') {
  notificationService.sendWhatsAppNotification(
    createdParty.id,
    createdParty.name,
    undefined,
    undefined,
    'ACCOUNT_ACTIVATION',
    `*K.K.CANVANSSING AGENT | REGISTRATION SUBMITTED*\n\n` +
    `WELCOME TO K.K.CANVANSSING AGENT, ${contactPersonUpper}.\n\n` +
    `YOUR ORGANIZATION *${partyNameUpper}* (${partyCodeUpper}) HAS BEEN REGISTERED.\n\n` +
    `👉 PARTY LOGIN LINK:\nhttps://kkcanvassing.com/party-login\n\n` +
    `LOGIN CREDENTIALS:\n` +
    `MOBILE NUMBER AS PASSWORD: ${firstAddedNo}\n\n` +
    `_K.K.CANVANSSING AGENT DESK • KKCANVASSING.COM_`,
    {
      phones: allPhones,
      portalLink: `/party-login`,
      viewLinkText: 'Go to Party Login'
    }
  );
}
```

*(When the Admin later approves the party via `PartyApprovalsPage.tsx` using `approveParty(id)`, `approveParty` will send the activation message as expected.)*

---

## 3. Verification Steps
1. Open the Registration Form (`/party-register`), fill out the details and select commodities (e.g. `Wheat`, `Soybean`), then submit.
2. Verify:
   * Record is saved in the database `parties` table with `status = 'PENDING_APPROVAL'`.
   * Records are created in `party_commodity_mapping` for each selected commodity.
   * Admin receives a WhatsApp notification about the new party registration.
164. Party immediately receives their login credentials and login link on their registered phone numbers.

---

## 4. Option A: Origin Manufacturer / Plant Origin on Resale Supplies

### Overview
In agricultural & commodity trading, when a **Buyer/Trader** purchases commodity stock from a **Manufacturer** (e.g. *Patidar Agro Processing Plant*) and later re-lists/sells the stock at updated market rates, the seller of the listing is now the Buyer/Trader, but **the product origin/manufacturer remains the original manufacturer**.

To preserve this provenance across resale cycles:
* `Supply` and `SupplyDTO` now expose:
  * `originManufacturerId?: number;` (ID of the original producing plant/manufacturer)
  * `originManufacturerName?: string;` (Company/plant name of the manufacturer)
* When a Manufacturer posts stock directly, the backend automatically sets `originManufacturerId = seller.id` and `originManufacturerName = seller.companyName`.
* When a Trader or secondary seller re-lists stock, they can pass `originManufacturerId` or `originManufacturerName` in `PostSupplyRequest`.

---

### Frontend Files to Update on the Other Laptop

#### File 1: `src/types/trading.ts` (or wherever `Supply` and `PostSupplyRequest` types are defined)

Update `Supply` interface:
```typescript
export interface Supply {
  id: number;
  supplyNumber: string;
  partyId: number;
  partyName: string;
  commodityName: string;
  parentCommodityName?: string;
  qualityName?: string;
  qualityGrade?: string;
  totalQuantity: number;
  availableQuantity: number;
  lockedQuantity: number;
  askingRate: number;
  location: string;
  originZone: string;
  originManufacturerId?: number;     // <--- ADD
  originManufacturerName?: string;   // <--- ADD
  maskedLabel?: string;
  labSpecs?: string;
  validTill: string;
  status: string;
  createdBy?: string;
  createdAt: string;
}
```

Update `PostSupplyRequest` interface:
```typescript
export interface PostSupplyRequest {
  commodityId: number;
  qualityGradeId?: number;
  totalQuantity: number;
  askingRate: number;
  location: string;
  originZone: string;
  originManufacturerId?: number;     // <--- ADD (optional)
  originManufacturerName?: string;   // <--- ADD (optional)
  labSpecs?: string;
  validTill: string;
}
```

---

#### File 2: Marketplace / Supply Cards UI (e.g. `src/pages/trading/MarketplacePage.tsx` or `src/components/trading/SupplyCard.tsx`)

Display the **Origin Manufacturer / Plant Origin** badge alongside the seller and location:

```tsx
{/* Origin Manufacturer / Plant Badge */}
{supply.originManufacturerName && (
  <div className="flex items-center gap-1.5 text-xs text-amber-700 bg-amber-50 border border-amber-200 px-2.5 py-1 rounded-md mt-1 w-fit">
    <span className="font-semibold">Plant / Manufacturer:</span>
    <span>{supply.originManufacturerName}</span>
  </div>
)}
```

In table view:
```tsx
<td className="px-4 py-2 text-sm text-gray-700">
  <div className="font-medium">{supply.commodityName}</div>
  {supply.originManufacturerName && (
    <div className="text-xs text-gray-500">
      Mfg: <span className="font-semibold text-gray-700">{supply.originManufacturerName}</span>
    </div>
  )}
</td>
```

---

#### File 3: Add/Post Supply Modal or Resale Form (e.g. `src/components/trading/PostSupplyModal.tsx`)

If the seller posting the lot is a Trader or reselling from an existing deal, allow them to select or pass the origin manufacturer:

```tsx
<div className="space-y-1">
  <label className="text-sm font-medium text-gray-700">
    Original Manufacturer / Plant Origin (Optional)
  </label>
  <select
    value={formData.originManufacturerId || ''}
    onChange={(e) => {
      const selectedId = e.target.value ? Number(e.target.value) : undefined;
      const selectedMfg = manufacturersList.find(m => m.id === selectedId);
      setFormData(prev => ({
        ...prev,
        originManufacturerId: selectedId,
        originManufacturerName: selectedMfg ? selectedMfg.companyName : undefined
      }));
    }}
    className="w-full border rounded-md px-3 py-2 text-sm"
  >
    <option value="">-- Direct / Default Seller --</option>
    {manufacturersList.map((mfg) => (
      <option key={mfg.id} value={mfg.id}>
        {mfg.companyName} {mfg.plantLocation ? `(${mfg.plantLocation})` : ''}
      </option>
    ))}
  </select>
  <p className="text-xs text-gray-400">
    If left empty and the posting party is a Manufacturer, it automatically sets the current party as origin.
  </p>
</div>
```

---

### Verification
1. Create a supply as a Manufacturer: notice `originManufacturerName` is automatically populated with the Manufacturer company name.
2. View Marketplace / Supplies list: notice the Manufacturer / Plant Origin is visible on the supply card/row even when viewed by buyers.
3. Resell as a Trader: choose an origin manufacturer; verify that the supply shows the trader as seller/contact, but retains the original manufacturer badge.
